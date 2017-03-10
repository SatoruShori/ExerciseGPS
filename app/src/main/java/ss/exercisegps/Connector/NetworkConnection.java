package ss.exercisegps.Connector;


import android.os.Environment;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import ss.exercisegps.R;
import ss.exercisegps.Utillities.SystemUtils;


/**
 * Created by Satoru on 9/4/2559.
 */
public class NetworkConnection {
    private static NetworkConnection networkConnection;

    private static Gson gson = new Gson();

    private static OkHttpClient client = new OkHttpClient();
    private static File rootFolder;

    private final MediaType postMediaType = MediaType.parse("text/x-markdown; charset=utf-8");
    
    private static Map<String,String> content = new HashMap<>();

    public interface OkHttpProgressListener {
        void onStarting(String fileName, long maxSize);
        void onProgress(String fileName, long maxSize, long currentSize);
        void onFinish(String fileName, long maxSize, long currentSize, File file);
        void onFailure(Exception e);
    }
    
    public static NetworkConnection getInstance() {
        if(networkConnection==null) {
            networkConnection = new NetworkConnection();
            client.newBuilder()
                    .connectTimeout(1000*60, TimeUnit.MILLISECONDS)
                    .writeTimeout(1000*60, TimeUnit.MILLISECONDS)
                    .readTimeout(1000*60, TimeUnit.MILLISECONDS);
        }
        content.clear();
        return networkConnection;
    }

    public NetworkConnection putData(Map<String,String> dataMap) {
        System.out.println(new Gson().toJson(dataMap));
        content.putAll(dataMap);
        return this;
    }

    public NetworkConnection putData(String name,String value) {
        System.out.println("name="+name);
        System.out.println("value="+value);
        content.put(name, value);
        return this;
    }

    public NetworkConnection putData(String name,Object value) {
        return putData(name, gson.toJson(value));
    }
    
    public NetworkConnection removeData(String name) {
    	content.remove(name);
    	return this;
    }
    
    public NetworkConnection clearData() {
    	content.clear();
    	return this;
    }

    private void doProcess(Request.Builder builder, final ResultData resultData) {
        Request request = builder.build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if(resultData!=null) {
                    resultData.onFailure(e);
                } else {
                    Log.e(this.getClass().getName(),e.getMessage());
                }
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    if (HttpURLConnection.HTTP_OK == response.code()) {
                        String result = response.body().string();
                        System.out.println("Result : " + result);
                        if (resultData != null) {
                            Object object = null;
                            try {
                                if (SystemUtils.isJSONValid(result)) {
                                    Type type = ((ParameterizedType) resultData.getClass().getGenericInterfaces()[0]).getActualTypeArguments()[0];
                                    object = SystemUtils.getGson().fromJson(result, type);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            } finally {
                                if (object == null) {
                                    object = result;
                                }
                                resultData.getData(object);
                            }
                        }
                    }
                } catch (IOException e) {
                    if(resultData != null) {
                        resultData.onFailure(e);
                    } else {
                        Log.e(this.getClass().getName(),e.getMessage());
                    }
                }
            }
        });
    }

    public interface ResultData<T> {
        void getData(T data);
        void onFailure(IOException e);
    }
    
    public void postDownload(String url, final OkHttpProgressListener okHttpProgressListener) {
    	Request.Builder builder = new Request.Builder().url(url);
        try {
//            content.setType(this.postMediaType);
        	FormBody.Builder formBuilder = new FormBody.Builder();
        	for(Entry<String,String> entry : content.entrySet()) {
        		formBuilder.add(entry.getKey(), entry.getValue());
        	}
            builder.post(formBuilder.build());
        } catch (IllegalStateException e){
//            builder.post(RequestBody.create(this.postMediaType, ""));
            e.printStackTrace();
        }
        client.newCall(builder.build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            	okHttpProgressListener.onFailure(e);
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    if (HttpURLConnection.HTTP_OK == response.code()) {
                    	ResponseBody responseBody = response.body();

                        long contentLength = responseBody.contentLength();
                        System.out.println("fileLength="+contentLength);
                        String filename = response.header("FileName");
                        System.out.println("filename="+filename);
                        String filepath = response.header("FilePath");
                        System.out.println("filepath="+filepath);
                        String md5 = response.header("MD5");
                        System.out.println("md5="+md5);
                        if(rootFolder==null) {
                            String appName = SystemUtils.getActivity().getString(R.string.app_name);
                        	rootFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), appName);
                        }
                        File folder = new File(rootFolder,filepath);
                        if(!folder.exists()) {
                            boolean re = folder.mkdirs();
                        }
                        File file = new File(folder,filename);
                        BufferedInputStream input = new BufferedInputStream(responseBody.byteStream());
                        OutputStream output = new FileOutputStream(file);
                        new DownloadProgress(md5, contentLength ,input,output,file,okHttpProgressListener);
                    } else {
                    	System.out.println("HTTP Code ="+response.code());
                    }
                } catch (IOException e) {
                	okHttpProgressListener.onFailure(e);
                }
            }
        });
    }
    
    private class DownloadProgress {



        public DownloadProgress(String md5, long contentLength,BufferedInputStream input,OutputStream output,File file,OkHttpProgressListener okHttpProgressListener) {
            String fileName = file.getName();
            long total = 0;
            int count;
            try {
                byte[] data = new byte[1024];
            	okHttpProgressListener.onStarting(fileName,contentLength);
                while ((count = input.read(data)) != -1) {
                    total += count;

                    okHttpProgressListener.onProgress(fileName,contentLength,total);
                    output.write(data, 0, count);
                }
                okHttpProgressListener.onFinish(fileName,contentLength,total,file);

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    output.flush();
                    output.close();
                    input.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void postRequest(String url, ResultData resultData) {
        System.out.println(url);
        Request.Builder builder = new Request.Builder().url(url);
        try {
        	FormBody.Builder formBuilder = new FormBody.Builder();
        	
        	for(Entry<String,String> entry : content.entrySet()) {
        		formBuilder.add(entry.getKey(), entry.getValue());
        	}
            builder.post(formBuilder.build());
        } catch (IllegalStateException e){
            builder.post(RequestBody.create(this.postMediaType, ""));
            e.printStackTrace();
        }
        doProcess(builder, resultData);
    }
    
}
