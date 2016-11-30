package nc.sgzw.uap.org.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import nc.bs.framework.common.RuntimeEnv;
import nc.bs.logging.Logger;
import nc.vo.pub.lang.UFDate;

/**
 * 文件工具类,<br>
 * 用于记录调用接口输入输出<br>
 * @author 唐浩翔<br>
 * 2015-3-22 上午11:03:50
 */
public class FileKit {
	private static  ExecutorService pool = Executors.newFixedThreadPool(4+4*Runtime.getRuntime().availableProcessors());
	private static FileKit fk=new FileKit();
	
	/**
	 * 写日志任务
	 *
	 * @param filepath 完整的日志文件路径
	 * @param loglist 日志
	 */
	private static void addTask(String filepath,List<String> loglist){
		PoolWorker work=fk.new PoolWorker(filepath,loglist);
		pool.execute(work);
	}
	
	/**
	 * 写日志任务
	 *
	 * @param loglist 日志
	 * @param code 接口编码
	 * @param syscode 外系统编码
	 */
	public static void addTask(List<String> loglist,String code,String syscode){
		try{
			String nchome=RuntimeEnv.getInstance().getNCHome();
			nchome=nchome.replace("\\", "/");
			if(!nchome.endsWith("/"))
				nchome += "/";
			String rq=new UFDate(System.currentTimeMillis()).toString().replace("-", "").substring(0, 8);
			String path="nclogs/tms/"+code+"/"+syscode+"/"+rq+"/"+UUID.randomUUID()+".txt";
			FileKit.addTask("E:/nchome/nclogs/sgzwapplog.txt", loglist);
		}catch(Exception ex){
			ex.printStackTrace();
			Logger.error(ex.getMessage(),ex);
		}
	}
	
	
	private class PoolWorker extends Thread {
		String filepath;
		List<String> str;
		public PoolWorker(String filepath,List<String> str){
			this.filepath=filepath.replace("\\", "/");
			this.str=str;
		}
		
		@Override
		public void run() {
			StringBuilder sb=new StringBuilder();
			for (String s : str) {
				sb.append(s).append("\n");
			}
			FileWriter writer;
			try {
				new File(filepath.substring(0, filepath.lastIndexOf("/"))).mkdirs();
				File f=new File(filepath);
				f.createNewFile();
				writer = new FileWriter(f,true);
				writer.write(sb.toString());
				
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
				Logger.error(e.getMessage());
			}
			
		}
		
	}
	

}
