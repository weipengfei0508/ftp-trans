package com.ztxy.dwps.file.service;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class FtpService {

	@Value("${ftp.host.url}")
	private String hostUrl;

	@Value("${ftp.host.username}")
	private String username;

	@Value("${ftp.host.password}")
	private String password;

	@Value("${ftp.host.port}")
	private int port;

	@Value("${ftp.upload.size}")
	private int uploadSize;
	

	public void downloadFiles(String ftpDir, String localPath) {
		FTPClient ftpClient = connectServer();
		if (ftpClient != null) {
			try {
				FTPFile[] ftpFiles = ftpClient.listFiles(ftpDir);
				if (ftpFiles == null || ftpFiles.length == 0) {
					//throw new FileNotFoundException("File " + ftpDir + " was not found on FTP server.");
					//log.debug("File " + ftpDir + " was not found on FTP server.");
					
				}else{
					for (int i = 0; ftpFiles != null && i < ftpFiles.length; i++) {
						FTPFile ftpFile = ftpFiles[i];
						if(ftpFile.isFile()){
							if(!ftpFile.getName().endsWith(".tmp")){
								//log.debug("即将下载文件："+ftpFile.getName());
								File downFile = new File(localPath + "/" + ftpFile.getName());
								if (ftpFile.getSize() > Integer.MAX_VALUE) {
									throw new IOException("File " + ftpFile.getName() + " is too large.");
								}
								downLoadFile(ftpDir, ftpFile.getName(), downFile, ftpClient, true);
							}
						}
					}
				}
				closeConnect(ftpClient);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			throw new RuntimeException("FTP连接失败！");
		}
	}

	/**
	 * 下载ftp文件
	 * 
	 * @param ftpDir
	 *            ftp路径
	 * @param ftpFileName
	 *            ftp文件名
	 * @param downFile
	 *            下截文件
	 * @param ftpClient
	 * @param isDelete
	 *            是否删除文件
	 * @return
	 */
	public boolean downLoadFile(String ftpDir, String ftpFileName, File downFile, FTPClient ftpClient,
			boolean isDelete) {
		boolean flag = false;
		try {
			OutputStream out = new FileOutputStream(downFile);
			String ftpFilePathName = ftpDir + "/" + ftpFileName;

			// 绑定输出流下载文件,需要设置编码集，不然可能出现文件为空的情况
			// boolean flag = ftpClient.retrieveFile(new
			// String(ftpFile.getName().getBytes("UTF-8"), "ISO-8859-1"),out);
			flag = ftpClient.retrieveFile(ftpFilePathName, out);
			out.flush();
			out.close();
			if (isDelete) {
				ftpClient.deleteFile(ftpFilePathName);
			}
//			if (flag) {
//				
//				// logger.info("下载成功");
//			} else {
//				// logger.error("下载失败");
//			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return flag;
	}

	/**
	 * 下载FTP下指定文件
	 * 
	 * @param ftp
	 *            FTPClient对象
	 * @param filePath
	 *            FTP文件路径
	 * @param fileName
	 *            文件名
	 * @param downPath
	 *            下载保存的目录
	 * @return
	 */
	public boolean downLoadFTP(FTPClient ftp, String filePath, String fileName, String downPath) {
		// 默认失败
		boolean flag = false;
		try {
			// 跳转到文件目录
			ftp.changeWorkingDirectory(filePath);
			// 获取目录下文件集合
			ftp.enterLocalPassiveMode();
			FTPFile[] files = ftp.listFiles();
			for (FTPFile file : files) {
				// 取得指定文件并下载
				if (file.getName().equals(fileName)) {
					File downFile = new File(downPath + File.separator + file.getName());
					OutputStream out = new FileOutputStream(downFile);
					// 绑定输出流下载文件,需要设置编码集，不然可能出现文件为空的情况
					flag = ftp.retrieveFile(new String(file.getName().getBytes("UTF-8"), "ISO-8859-1"), out);
					// 下载成功删除文件,看项目需求
					// ftp.deleteFile(new
					// String(fileName.getBytes("UTF-8"),"ISO-8859-1"));
					out.flush();
					out.close();
					if (flag) {
						// logger.info("下载成功");
					} else {
						// logger.error("下载失败");
					}
				}
			}

		} catch (Exception e) {
			// logger.error("下载失败");
		}

		return flag;
	}

	/**
	 * 下载ftp文件到本地上
	 *
	 * @param ftpFileName
	 *            ftp文件路径名称
	 * @param localFile
	 *            本地文件路径名称
	 */
	public void download(String ftpFileName, File localFile, FTPClient ftpClient) throws IOException {
		OutputStream out = null;
		try {
			FTPFile[] fileInfoArray = ftpClient.listFiles(ftpFileName);
			if (fileInfoArray == null || fileInfoArray.length == 0) {
				throw new FileNotFoundException("File " + ftpFileName + " was not found on FTP server.");
			}

			FTPFile fileInfo = fileInfoArray[0];
			if (fileInfo.getSize() > Integer.MAX_VALUE) {
				throw new IOException("File " + ftpFileName + " is too large.");
			}

			out = new BufferedOutputStream(new FileOutputStream(localFile));
			if (!ftpClient.retrieveFile(ftpFileName, out)) {
				throw new IOException(
						"Error loading file " + ftpFileName + " from FTP server. Check FTP permissions and path.");
			}
			out.flush();
		} finally {
			closeStream(out);
		}
	}

	/**
	 * 关闭流
	 *
	 * @param stream
	 *            流
	 */
	private static void closeStream(Closeable stream) {
		if (stream != null) {
			try {
				stream.close();
			} catch (IOException ex) {
				// do nothing
			}
		}
	}

	/**
	 * 上传数据文件到Ftp
	 */
	public void updateFtpFile(String src, String des) {
		FTPClient ftpClient = connectServer();
		if (ftpClient != null) {
			try {
				String remoteDir = des.substring(0, des.lastIndexOf("/"));
				mkDirecrotys(ftpClient, remoteDir);
				uploadFile(ftpClient, src, des);
				closeConnect(ftpClient);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			throw new RuntimeException("FTP连接失败！");
		}
	}

	/**
	 * 连接服务器
	 */
	public FTPClient connectServer() {
		FTPClient ftpClient = null;
		try {
			ftpClient = new FTPClient();
			ftpClient.connect(hostUrl, port);
			int reply = ftpClient.getReplyCode();
			// 以2开头的返回值就会为真
			if (!FTPReply.isPositiveCompletion(reply)) {
				closeConnect(ftpClient);
				log.error("FTP server refused connection.");
				throw new RuntimeException("FTP server refused connection:" + ftpClient.getReplyString());
			}

			boolean islogin = ftpClient.login(username, password);
			if (!islogin) {
				closeConnect(ftpClient);
				throw new IOException("Can't login to server :" + hostUrl);
			}

			//// 如果缺省该句 传输txt正常 但图片和其他格式的文件传输可能会出现未知异常
			// ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
			FTPClientConfig conf = new FTPClientConfig(FTPClientConfig.SYST_UNIX);
			conf.setServerLanguageCode("zh");
			ftpClient.configure(conf);
			ftpClient.setControlEncoding("UTF-8");
			ftpClient.enterLocalPassiveMode();
			ftpClient.setBufferSize(uploadSize);

		} catch (SocketException e) {
			e.printStackTrace();
			throw new RuntimeException("FTP连接失败！", e);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("FTP客户端出错！", e);
		}
		return ftpClient;
	}

	/**
	 * 关闭链接
	 */
	public void closeConnect(FTPClient ftpClient) {
		if (null != ftpClient && ftpClient.isConnected()) {
			try {
				ftpClient.logout();
				ftpClient.disconnect();
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException("关闭FTP连接发生异常！", e);
			}
		}
	}

	/**
	 * 改变目录路径
	 * 
	 * @param ftpClient
	 * @param directory
	 * @return
	 */
	public boolean changeWorkingDirectory(FTPClient ftpClient, String directory) {
		boolean flag = true;
		try {
			flag = ftpClient.changeWorkingDirectory(directory);
			/*
			 * if (flag) { log.debug("进入文件夹" + directory + " 成功！"); } else {
			 * log.debug("进入文件夹" + directory + " 失败！"); }
			 */
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return flag;
	}

	/**
	 * 上传文件
	 * 
	 * @param ftpClient
	 * @param src
	 * @param des
	 */
	public void uploadFile(FTPClient ftpClient, String src, String des) {
		File srcFile = new File(src);
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(srcFile);
			ftpClient.storeFile(des, fis);
			// ftpClient.rename(des, des.replace(".tmp", ".txt"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException("文件未找到！", e);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("FTP客户端出错！", e);
		} finally {
			IOUtils.closeQuietly(fis);
		}
	}

	/**
	 * 创建多层目录文件，如果有ftp服务器已存在该文件，则不创建，如果无，则创建
	 * 
	 * @param remote
	 * @return
	 * @throws IOException
	 */
	public boolean mkDirecrotys(FTPClient ftpClient, String remote) throws IOException {
		boolean success = true;
		String directory = remote + "/";
		// String directory = remote.substring(0, remote.lastIndexOf("/") + 1);
		// 如果远程目录不存在，则递归创建远程服务器目录
		if (!directory.equalsIgnoreCase("/") && !changeWorkingDirectory(ftpClient, new String(directory))) {
			int start = 0;
			int end = 0;
			if (directory.startsWith("/")) {
				start = 1;
			} else {
				start = 0;
			}
			end = directory.indexOf("/", start);
			String path = "";
			String paths = "";
			while (true) {
				String subDirectory = new String(remote.substring(start, end).getBytes("utf-8"), "utf-8");
				path = path + "/" + subDirectory;
				if (!existFile(ftpClient, path)) {
					if (mkdir(ftpClient, subDirectory)) {
						changeWorkingDirectory(ftpClient, subDirectory);
					} else {
						changeWorkingDirectory(ftpClient, subDirectory);
					}
				} else {
					changeWorkingDirectory(ftpClient, subDirectory);
				}

				paths = paths + "/" + subDirectory;
				start = end + 1;
				end = directory.indexOf("/", start);
				// 检查所有目录是否创建完毕
				if (end <= start) {
					break;
				}
			}
		}
		return success;
	}

	/**
	 * 判断ftp服务器文件是否存在
	 * 
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public boolean existFile(FTPClient ftpClient, String path) throws IOException {
		boolean flag = false;
		FTPFile[] ftpFileArr = ftpClient.listFiles(path);
		if (ftpFileArr.length > 0) {
			flag = true;
		}
		return flag;
	}

	/**
	 * 在 ftp服务器上创建目录
	 * 
	 * @param dir
	 */
	public boolean mkdir(FTPClient ftpClient, String dir) {
		boolean flag = true;
		try {
			flag = ftpClient.makeDirectory(dir);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("FTP发生异常！", e);
		}
		return flag;
	}

	public static void main(String[] args) {
		// FTPClient ftpClient = null;
		// try {
		// ftpClient = new FTPClient();
		// ftpClient.connect("192.168.1.148",21);
		// int reply = ftpClient.getReplyCode();
		// if (!FTPReply.isPositiveCompletion(reply)) {
		// //disconnect();
		// throw new IOException("Can't connect to server :" + "192.168.1.148");
		// }
		//
		// //ftpClient.login("efence", "Efenece@2019");
		// // After connection attempt, you should check the reply code to
		// // verify
		// // success.
		// boolean n=ftpClient.login("efence", "Efence@2019");
		//// int reply = ftpClient.getReplyCode();
		////
		//// // 以2开头的返回值就会为真
		//// if (!FTPReply.isPositiveCompletion(reply)) {
		//// ftpClient.disconnect();
		//// log.error("FTP server refused connection.");
		//// throw new RuntimeException("FTP server refused connection:" +
		// ftpClient.getReplyString());
		//// }
		// //// 如果缺省该句 传输txt正常 但图片和其他格式的文件传输可能会出现未知异常
		// // ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
		// FTPClientConfig conf = new
		// FTPClientConfig(FTPClientConfig.SYST_UNIX);
		// conf.setServerLanguageCode("zh");
		//
		// ftpClient.configure(conf);
		// ftpClient.setControlEncoding("UTF-8");
		// ftpClient.enterLocalPassiveMode();
		// ftpClient.setBufferSize(5050);
		// FtpService ftpService = new FtpService();
		// ftpService.mkDirecrotys(ftpClient, "1");
		// ftpClient.login("efence", "Efence@2019");
		// ftpClient.login("efence", "Efence@2019");
		// ftpService.uploadFile(ftpClient, "C:\\tmp\\i_01-20191213094859.txt",
		// "/IMSI/20191213/09/i_01-20191213095302-094A891AC364C77FEB7CF13130E0A828.tmp");
		// } catch (SocketException e) {
		// e.printStackTrace();
		// throw new RuntimeException("FTP连接失败！", e);
		// } catch (IOException e) {
		// e.printStackTrace();
		// throw new RuntimeException("FTP客户端出错！", e);
		// }

		// FTPClient ftpClient = null;
		// try {
		// FTPClient ftpClient = new FTPClient();
		// ftpClient.connect("192.168.1.148");
		//
		// // After connection attempt, you should check the reply code to
		// // verify
		// // success.
		// int reply = ftpClient.getReplyCode();
		// // 以2开头的返回值就会为真
		// if (!FTPReply.isPositiveCompletion(reply)) {
		// ftpClient.disconnect();
		// log.error("FTP server refused connection.");
		// throw new RuntimeException("FTP server refused connection:" +
		// ftpClient.getReplyString());
		// }
		// boolean islogin=ftpClient.login("efence", "Efence@2019");
		// if (!islogin) {
		// ftpClient.disconnect();
		// throw new IOException("Can't login to server :" + "192.168.1.148");
		// }
		//
		//
		// //// 如果缺省该句 传输txt正常 但图片和其他格式的文件传输可能会出现未知异常
		// // ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
		// FTPClientConfig conf = new
		// FTPClientConfig(FTPClientConfig.SYST_UNIX);
		// conf.setServerLanguageCode("zh");
		//
		// ftpClient.configure(conf);
		// ftpClient.setControlEncoding("UTF-8");
		// ftpClient.enterLocalPassiveMode();
		// ftpClient.setBufferSize(5010);
		// FtpService ftpService = new FtpService();
		// ftpService.mkDirecrotys(ftpClient, "4");
		// } catch (SocketException e) {
		// e.printStackTrace();
		// throw new RuntimeException("FTP连接失败！", e);
		// } catch (IOException e) {
		// e.printStackTrace();
		// throw new RuntimeException("FTP客户端出错！", e);
		// }

	}

	/**
	 * 连接到ftp
	 */
	public static void connect() throws IOException {
		FTPClient ftpClient = new FTPClient();
		try {
			ftpClient.connect("192.168.1.148");
		} catch (UnknownHostException e) {
			throw new IOException("Can't find FTP server :" + "192.168.1.148");
		}

		int reply = ftpClient.getReplyCode();
		if (!FTPReply.isPositiveCompletion(reply)) {
			// disconnect();
			throw new IOException("Can't connect to server :" + "192.168.1.148");
		}

		if (!ftpClient.login("efence", "Efence@2019")) {
			// disconnect();
			throw new IOException("Can't login to server :" + "192.168.1.148");
		}

		// set data transfer mode.
		// ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

		// Use passive mode to pass firewalls.
		// ftpClient.enterLocalPassiveMode();

		// initFtpBasePath();
	}

	// public static void main(String[] args) {
	// FTPClient ftp = new FTPClient();
	// FTPClientConfig config = new FTPClientConfig();
	// //config.set
	// // config.setXXX(YYY); // change required options
	// // for example config.setServerTimeZoneId("Pacific/Pitcairn")
	// // ftp.configure(config );
	// boolean error = false;
	// try {
	// int reply;
	// String server = "192.168.1.148";
	// ftp.connect(server);
	// System.out.println("Connected to " + server + ".");
	// System.out.print(ftp.getReplyString());
	//
	// // After connection attempt, you should check the reply code to verify
	// // success.
	// reply = ftp.getReplyCode();
	//
	// if(!FTPReply.isPositiveCompletion(reply)) {
	// ftp.disconnect();
	// System.err.println("FTP server refused connection.");
	// System.exit(1);
	// }
	// ftp.logout();
	// } catch(IOException e) {
	// error = true;
	// e.printStackTrace();
	// } finally {
	// if(ftp.isConnected()) {
	// try {
	// ftp.disconnect();
	// } catch(IOException ioe) {
	// // do nothing
	// }
	// }
	// System.exit(error ? 1 : 0);
	// }
	// }
}
