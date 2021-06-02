package Main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public class sync {

	public static Log log;
	public static boolean previewMode = false;
	private final static String accessKey = "1CFCAF9875058CB03406";
	private final static String secretKey = 
"WzI3QzFEMjJBMDc5ODdDQ0M2QkZDOUZCNTY1QkNG";
	public static void main(String[] args) throws FileNotFoundException,
			IllegalArgumentException, IOException {

		log = LogFactory.getLog(sync.class);

		AWSCredentials credentials = new PropertiesCredentials(new File(args[2]));
		@SuppressWarnings("deprecation")
		AmazonS3 client = new AmazonS3Client(credentials);

		if (args.length > 3 && args[3].equals("preview")) previewMode = true;
		syncing(client, args[1], new File(args[0]));

		System.out.println("All done.");
		
	}
	
	public static void syncing(AmazonS3 client, String bucketName, File root) {
		
		System.out.println("Syncing " + root + " to " + bucketName);
		
		log.info("Getting local file list...");
		Map<String, File> fileMap = new HashMap<String, File>();
		Map<String, String> localFiles = getLocalFiles(root, fileMap);
		
		log.info("Loading object listing...");
		Map<String, String> serverFiles = getServerFiles(client, bucketName);
		
		for (Map.Entry<String, String> entry : localFiles.entrySet()) {
			if (serverFiles.containsKey(entry.getKey())
					&& serverFiles.get(entry.getKey()).equals(entry.getValue())) {
				// file is up to date
			} else {
				System.out.print(
						(serverFiles.containsKey(entry.getKey()) ? "M" : "A")
						+ " " + entry.getKey());
				if (!previewMode) upload(client, bucketName, entry.getKey(), fileMap.get(entry.getKey()), accessKey, secretKey, root.getPath());
				System.out.println();
			}
		}
		
		for (Map.Entry<String, String> entry : serverFiles.entrySet()) {
			if (!localFiles.containsKey(entry.getKey())) {
				System.out.print("D " + entry.getKey());
				if (!previewMode) client.deleteObject(bucketName, entry.getKey());
				System.out.println();
			}
		}

	}

	public static void upload(AmazonS3 client, String bucketName, String key, File file, String accessKey, String secretKey, String filePath) {
		PutObjectRequest request = new PutObjectRequest(bucketName, key, file);
		ObjectMetadata metadata = new ObjectMetadata();
		if (file.getName().endsWith(".html")) {
			metadata.setContentType("text/html; charset=utf-8");
			System.out.print(" [html]");
		}
		
		if (checkFileSize(file, 20, "M")) {
			request.setMetadata(metadata); 
			client.putObject(request);
			System.out.println("Upload Done!");
		} else {
			MultipartUpload mpul = new MultipartUpload(bucketName, accessKey, secretKey, filePath);
			mpul.upload();
		}
		
	}
	
	public static Map<String, String> getServerFiles(AmazonS3 client, String bucketName) {

		ObjectListing listing;
		Map<String, String> files = new TreeMap<String, String>();

		ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
				.withBucketName(bucketName);

		do {
			listing = client.listObjects(listObjectsRequest);
			for (S3ObjectSummary summary : listing.getObjectSummaries()) {
				files.put(summary.getKey(), summary.getETag());
			}
		} while (listing.isTruncated());

		return files;

	}

	public static Map<String, String> getLocalFiles(File root, Map<String, File> fileMap) {

		Map<String, String> files = new TreeMap<String, String>();

		getLocalFilesRecursive(root, files, fileMap, "");

		return files;

	}

	public static void getLocalFilesRecursive(File dir,
			Map<String, String> list, Map<String, File> fileMap, String prefix) {

		for (File file : dir.listFiles()) {

			if (file.isFile()) {
				if (file.getName().equals("Thumbs.db")) continue;
				if (file.getName().startsWith(".")) continue;
				String key = prefix + file.getName();
				key = key.replaceFirst("\\.noex\\.[^\\.]+$", "");
				list.put(key, getMD5Checksum(file));
				fileMap.put(key, file);
			} else if (file.isDirectory()) {
				getLocalFilesRecursive(file, list, fileMap, prefix + file.getName() + "/");
			}

		}

	}

	public static boolean checkFileSize(File file, int size, String unit) {
		long len = file.length();
		double fileSize = 0;
		if ("B".equals(unit.toUpperCase())) {
			fileSize = (double) len;
		} else if ("K".equals(unit.toUpperCase())) {
			fileSize = (double) len / 1024;
		} else if ("M".equals(unit.toUpperCase())) {
			fileSize = (double) len / 1048576;
		} else if ("G".equals(unit.toUpperCase())) {
			fileSize = (double) len / 1073741824;
		}
		
		if (fileSize > size) {
			return false;
		}
		return true;
	}
	
	public static byte[] createChecksum(File file) throws Exception {
		InputStream fis = new FileInputStream(file);

		byte[] buffer = new byte[1024];
		MessageDigest complete = MessageDigest.getInstance("MD5");
		int numRead;

		do {
			numRead = fis.read(buffer);
			if (numRead > 0) {
				complete.update(buffer, 0, numRead);
			}
		} while (numRead != -1);

		fis.close();
		return complete.digest();
	}

	public static String getMD5Checksum(File file) {
		byte[] b;
		try {
			b = createChecksum(file);
		} catch (Exception e) {
			return "";
		}
		
		String result = "";

		for (int i = 0; i < b.length; i++) {
			result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
		}
		return result;
	}

}
