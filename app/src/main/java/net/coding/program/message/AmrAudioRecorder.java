package net.coding.program.message;

import android.media.AudioRecord;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by Carlos2015 on 2015/8/28.
 */
public class AmrAudioRecorder {
	private final static int[] sampleRates = {44100, 22050, 11025, 8000};
	// Used only in uncompressed mode
	private static final int BUFFER_FRAME_SIZE =960; 
	private String outPath;
	private int sampleRate;
	private int bufferSize;
	private AudioRecord audioRecorder;
	private State state;
	// Number of frames written to file on each output(only in uncompressed
		// mode)
	private int framePeriod;
	private byte[] buffer;
	private boolean isAddAmrFileHead;
	private boolean isRecording;
	private long duration = 0l;
	private final Object mLock = new Object();
	private String TAG = "AmrAudioRecorder";
	private int audioSource;
	private int channelConfig;
	private int audioFormat;

	public AmrAudioRecorder(int audioSource, int channelConfig,int audioFormat,String outPath){
		this.outPath = outPath;
		this.audioSource = audioSource;
		this.channelConfig = channelConfig;
		this.audioFormat = audioFormat;
		try {
			sampleRate = sampleRates[3];
			bufferSize = BUFFER_FRAME_SIZE;  
			  
//			bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig,
//	                audioFormat);
			bufferSize = 1024*2;
			if (bufferSize == AudioRecord.ERROR_BAD_VALUE) {  
	            Log.e("AmrAudioRecorder", "bufferSize error");  
	            return;  
	        }
			initAudioRecoder(audioSource, channelConfig, audioFormat);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void initAudioRecoder(int audioSource, int channelConfig, int audioFormat) throws Exception {
		audioRecorder = new AudioRecord(audioSource, sampleRate, channelConfig, audioFormat, bufferSize);
		if (audioRecorder.getState() != AudioRecord.STATE_INITIALIZED)
            throw new Exception("AudioRecord initialization failed");
		buffer = new byte[bufferSize];
		state = State.INITIALIZING;
		Log.w("AmrAudioRecorder", "sampleRate=" + sampleRate + ",bufferSize=" + bufferSize);
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	/**
	 * INITIALIZING : recorder is initializing; READY : recorder has been
	 * initialized, recorder not yet started RECORDING : recording ERROR :
	 * reconstruction needed STOPPED: reset needed
	 */
	public enum State {
		INITIALIZING, READY, RECORDING, ERROR, STOPPED
	}
	
	public void prepare(){
		if (state == State.INITIALIZING) {
			if ((audioRecorder.getState() == AudioRecord.STATE_INITIALIZED)
					& (outPath != null)) {
				File file = new File(outPath).getParentFile();
				if(file!=null && !file.exists()){
					file.mkdirs();
				}
				isAddAmrFileHead = true;
				duration = 0l;
				state = State.READY;
			}else{
				state = State.ERROR;
			}
		}
	}

	private VoiceRecordingCallBack mVoiceRecordingCallBack;

	public void setVoiceRecordingCallBack(VoiceRecordingCallBack mVoiceRecordingCallBack){
		this.mVoiceRecordingCallBack = mVoiceRecordingCallBack;
	}

	public void pause(){
		stop();
	}

	public long getDuration(){
		return duration;
	}

	public void continueRecord() throws Exception {
		if(state != State.ERROR){
			initAudioRecoder(audioSource, channelConfig, audioFormat);
			state = State.READY;
			isAddAmrFileHead = false;
			start();
		}
	}



	
	/**
	 * 
	 * 
	 * Starts the recording, and sets the state to RECORDING. Call after
	 * prepare().
	 * 
	 */
	public void start() {
		if (state == State.READY) {
				isRecording = true;
				audioRecorder.startRecording();
				state = State.RECORDING;
				new Thread(){
					public void run() {
						ByteArrayOutputStream bos = new ByteArrayOutputStream();
						long startTime = System.currentTimeMillis();
						long endTime = 0l;
						int len = 0;
						synchronized (mLock) {
							mLock.notify();
						}
						while(isRecording && state == AmrAudioRecorder.State.RECORDING && (AudioRecord.ERROR_INVALID_OPERATION!= (len =audioRecorder.read(buffer, 0, buffer.length)))){
							endTime = System.currentTimeMillis();
							try {
								bos.write(buffer,0,len);
								long v = 0;
								// 将 buffer 内容取出，进行平方和运算
								for (int i = 0; i < len; i++) {
									v += buffer[i] * buffer[i];
								}
								// 平方和除以数据总长度，得到音量大小。
								double mean = v / (double) len;
								double volume = 10 * Math.log10(mean);
								duration +=(endTime - startTime);
								startTime = endTime;
								Log.w(TAG, "duration:" + duration + ",分贝值:" + volume);
								if(mVoiceRecordingCallBack!=null){
									mVoiceRecordingCallBack.onRecord(duration,volume);
								}
								if(isRecording){
									// 大概一秒十次
									synchronized (mLock) {
										try {
											mLock.wait(100);
										} catch (InterruptedException e) {
											e.printStackTrace();
										}
									}
								}

							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						try {
							byte[] pcm = bos.toByteArray();
//							VoiceDenoiseUtils mVoiceDenoiseUtils = new VoiceDenoiseUtils();
//							mVoiceDenoiseUtils.initAudioDeNose();
//							mVoiceDenoiseUtils.audioDeNose8K(pcm);
//							mVoiceDenoiseUtils.exitAudioDeNose();
							ByteArrayInputStream bis = new ByteArrayInputStream(pcm);
							bos.close();

							if(bis.available()!=0){
								byte[] data = AmrUtils.convertToAmr(bis, isAddAmrFileHead);
								File f = new File(outPath);
								if(!f.exists()){
									f.createNewFile();
								}
								RandomAccessFile raf =new RandomAccessFile(f,"rw");
								if(!isAddAmrFileHead){
									raf.seek(f.length());
								}
								raf.write(data,0,data.length);
								raf.close();
//								if(isAddAmrFileHead){
//									FileOutputStream fos = new FileOutputStream(outPath,true);
//									Log.w(TAG,"fileSize:"+data.length);
//									fos.write(data);
//									fos.flush();
//									fos.close();
//								}else{
//									File f = new File(outPath);
//									if(!f.exists()){
//										f.createNewFile();
//									}
//									RandomAccessFile raf =new RandomAccessFile(f,"rw");
//									raf.seek(f.length());
//									raf.write(data,0,data.length);
//									raf.close();
//
//								}

							}
						} catch (IllegalArgumentException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (ClassNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (NoSuchMethodException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (InstantiationException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (InvocationTargetException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}finally {
							synchronized (mLock) {
								mLock.notify();
							}
							if(audioRecorder!=null){
								audioRecorder.stop();
								audioRecorder.release();
								audioRecorder = null;
							}
						}
					}
				}.start();
		} else {
			Log.e("start()",
					"start() called on illegal state");
			state = State.ERROR;
		}
	}
	
	public void stop(){
		state = State.STOPPED;
		isRecording = false;
		synchronized (mLock) {
			try {
				mLock.wait(1800);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}


	public interface VoiceRecordingCallBack{
		void onRecord(long duration,double volume);
	}

}
