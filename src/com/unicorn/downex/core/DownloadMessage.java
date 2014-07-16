/*
 * Copyright (C) 2014 The DownEx Project of Unicorn
 *
 * muzzyhorse
 */
package com.unicorn.downex.core;

import java.io.IOException;
import java.util.ArrayList;

import android.util.JsonReader;
import android.util.JsonWriter;

/**
 * @author xuchunlei
 *
 */
public class DownloadMessage {

    /**
     * 
     */
    private DownloadMessage() {
        
    }
    
    /**
     * 消息发送接口
     * @author xu
     *
     */
    public interface IMessage {
    	void send(JsonWriter writer) throws IOException;
    	void receive(JsonReader reader) throws IOException;
    }
    
    /**
     * 字符串消息-用于发送或接收单个字符串
     * @author xuchunlei
     *
     */
    public static class StringMessage implements IMessage {

        public String value;

        public StringMessage() {
            
        }
        
        public StringMessage(String value) {
            this.value = value;
        }
        
		@Override
		public void send(JsonWriter writer) throws IOException {
			writer.beginObject();
			writer.name("value").value(value);
			writer.endObject();
			writer.flush();
		}
		@Override
		public void receive(JsonReader reader) throws IOException {
			reader.beginObject();
			while(reader.hasNext()) {
				String name = reader.nextName(); 
				if(name.equals("value")) {
					value = reader.nextString();
				}
			}
			reader.endObject();
		}
    }    

    /**
     * 布尔值消息-用于发送或接收一个布尔值
     * @author xuchunlei
     *
     */
    public static class BooleanMessage implements IMessage {

        public volatile boolean value;

		@Override
		public void send(JsonWriter writer) throws IOException {
			writer.beginObject();
			writer.name("value").value(value);
			writer.endObject();
			writer.flush();
		}
		@Override
		public void receive(JsonReader reader) throws IOException {
			reader.beginObject();
			while(reader.hasNext()) {
				String name = reader.nextName(); 
				if(name.equals("value")) {
					value = reader.nextBoolean();
				}
			}
			reader.endObject();
		}
    }    

   /* *//**
     * 整型值消息-用于发送或接收一个整型值
     * @author xuchunlei
     *
     */
    public static class IntegerMessage implements IMessage {

        public int value;

        @Override
        public void send(JsonWriter writer) throws IOException {
            writer.beginObject();
            writer.name("value").value(value);
            writer.endObject();
            writer.flush();
        }
        @Override
        public void receive(JsonReader reader) throws IOException {
            reader.beginObject();
            while(reader.hasNext()) {
                String name = reader.nextName(); 
                if(name.equals("value")) {
                    value = reader.nextInt();
                }
            }
            reader.endObject();
        }
    }    
    
    /**
     * 请求消息-开始下载
     * @author xuchunlei
     *
     */
    public static class StartReqMessage implements IMessage {
        /** 下载url */
        public String url;
        /** 保存下载文件的目录 */
        public String dir;
        @Override
        public void send(JsonWriter writer) throws IOException {
            writer.beginObject();
            writer.name("url").value(url);
            writer.name("dir").value(dir);
            writer.endObject();
            writer.flush();
        }
        @Override
        public void receive(JsonReader reader) throws IOException {
            reader.beginObject();
            while(reader.hasNext()) {
                String name = reader.nextName(); 
                if(name.equals("url")) {
                    url = reader.nextString();
                } else if(name.equals("dir")) {
                    dir = reader.nextString();
                }
            }
            reader.endObject();
        }
    }
    /**
     * 响应消息-开始下载
     * @author xuchunlei
     *
     */
    public static class StartResMessage implements IMessage {
        /** 服务端生成的一个标识下载项的键值 */
        public String key;

        @Override
        public void receive(JsonReader reader) throws IOException {
            reader.beginObject();
            while(reader.hasNext()) {
                String name = reader.nextName(); 
                if(name.equals("key")) {
                    key = reader.nextString();
                }
            }
            reader.endObject();
        }

        @Override
        public void send(JsonWriter writer) throws IOException {
            writer.beginObject();
            writer.name("key").value(key);
            writer.endObject();
            writer.flush();
        }
    }
    
    /**
     * 下载进度消息
     * @author xuchunlei
     *
     */
    public static class ProgressMessage implements IMessage {

        public String key;
        public long currentBytes;
        public long totalBytes;
        public int status;
        public long retryAfter;
        
        @Override
        public void send(JsonWriter writer) throws IOException {
            writer.beginObject();
            writer.name("key").value(key);
            writer.name("currentBytes").value(currentBytes);
            writer.name("totalBytes").value(totalBytes);
            writer.name("status").value(status);
            writer.name("retryAfter").value(retryAfter);
            writer.endObject();
        }

        @Override
        public void receive(JsonReader reader) throws IOException {
            reader.beginObject();
            while(reader.hasNext()) {
                String name = reader.nextName(); 
                if(name.equals("key")) {
                    key = reader.nextString();
                } else if(name.equals("currentBytes")) {
                    currentBytes = reader.nextLong();
                } else if(name.equals("totalBytes")) {
                    totalBytes = reader.nextLong();
                } else if(name.equals("status")) {
                    status = reader.nextInt();
                } else if(name.equals("retryAfter")) {
                    retryAfter = reader.nextLong();
                }
            }
            reader.endObject();
        }
    }
    
    @SuppressWarnings("serial")
    public static class ProgressMessageList extends ArrayList<ProgressMessage> implements IMessage {

        @Override
        public void send(JsonWriter writer) throws IOException {
            writer.beginArray();
            for(ProgressMessage msg : this) {
                msg.send(writer);
            }
            writer.endArray();
            writer.flush();
        }

        @Override
        public void receive(JsonReader reader) throws IOException {
            reader.beginArray();
            while(reader.hasNext()) {
                ProgressMessage msg = new ProgressMessage();
                msg.receive(reader);
                add(msg);
            }
            reader.endArray();
        }
    }
}
