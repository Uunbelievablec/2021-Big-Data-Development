package Main;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
//import java.util.Map;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.AbortMultipartUploadRequest;
import com.amazonaws.services.s3.model.CompleteMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadRequest;
import com.amazonaws.services.s3.model.PartETag;
import com.amazonaws.services.s3.model.UploadPartRequest;

public class MultipartUpload {
	String bucketName;
	String accessKey;
	String secretKey;
	String localFilePath;
	String keyName = Paths.get(localFilePath).getFileName().toString();
	
	private final static String serviceEndpoint = "scut.depts.bingosoft.net:29997";;
	private final static String signingRegion = "";
	public static int MEGABYTE = 1024 * 1024;
	private int chunkSize = 20 * MEGABYTE;
	
	final BasicAWSCredentials credentials = 
			new BasicAWSCredentials(accessKey,secretKey);
					final ClientConfiguration ccfg = new ClientConfiguration().
							withUseExpectContinue(true);

					final EndpointConfiguration endpoint = 
			new EndpointConfiguration(serviceEndpoint, signingRegion);

					final AmazonS3 s3 = AmazonS3ClientBuilder.standard()
			                .withCredentials(new AWSStaticCredentialsProvider(credentials))
			                .withClientConfiguration(ccfg)
			                .withEndpointConfiguration(endpoint)
			                .withPathStyleAccessEnabled(true)
			                .build();
					
	MultipartUpload(String bucket, String awsAccessKeyId,
			String secretKey, String localFilePath) {
		bucketName = bucket;
		accessKey = awsAccessKeyId;
		//this.keyName = key;
		this.secretKey = secretKey;
		this.localFilePath = localFilePath;
	}
	
	ArrayList<PartETag> partETags = new ArrayList<PartETag>();
	File file = new File(localFilePath);
	long contentLength = file.length();
	String uploadId = null;
	public void upload() {
		try {
			initialize();
			uploadParts();
			System.out.println("Completing upload");
			CompleteMultipartUploadRequest compRequest = 
					new CompleteMultipartUploadRequest(bucketName, keyName, uploadId, partETags);

			s3.completeMultipartUpload(compRequest);
		} catch(Exception e) {
			System.err.println(e.toString());
			if (uploadId != null && !uploadId.isEmpty()) {
				// Cancel when error occurred
				System.out.println("Aborting upload");
				s3.abortMultipartUpload(new AbortMultipartUploadRequest(bucketName, keyName, uploadId));
			}
			System.exit(1);
		}
		System.out.println("Multipart Upload Done!");
		
	}
	public void initialize() {
		InitiateMultipartUploadRequest initRequest = 
				new InitiateMultipartUploadRequest(bucketName, keyName);
		uploadId = s3.initiateMultipartUpload(initRequest).getUploadId();
		System.out.format("Created upload ID was %s\n", uploadId);
	}
	public void uploadParts() {
		long filePosition = 0;
		for (int i = 1; filePosition < contentLength; i++) {
			// Last part can be less than 5 MB. Adjust part size.
			chunkSize = (int) Math.min(chunkSize, contentLength - filePosition);

			// Create request to upload a part.
			UploadPartRequest uploadRequest = new UploadPartRequest()
					.withBucketName(bucketName)
					.withKey(keyName)
					.withUploadId(uploadId)
					.withPartNumber(i)
					.withFileOffset(filePosition)
					.withFile(file)
					.withPartSize(chunkSize);

			// Upload part and add response to our list.
			System.out.format("Uploading part %d\n", i);
			partETags.add(s3.uploadPart(uploadRequest).getPartETag());

			filePosition += chunkSize;}
	}
}
