package com.google.cloud.vision.samples;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

import javax.imageio.ImageIO;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionScopes;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.AnnotateImageResponse;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.google.common.collect.ImmutableList;

/**
 * A sample application that uses the Vision API to detect landmarks in an image
 * that is hosted on Google Cloud Storage.
 * 
 * @author bumjong
 *
 */
public class DetectLandmark {

	/**
	 * Be sure to specify the name of your application. If the application name
	 * is {@code null} or blank, the application will log a warning. Suggested
	 * format is "MyCompany-ProductName/1.0".
	 */
	private static final String APPLICATION_NAME = "Google-VisionDetectLabel/1.0";

	private static final int MAX_RESULTS = 4;

	// [START run_application]
	/**
	 * Annotates an image using the Vision API.
	 */
	public static void main(String[] args) throws IOException,
			GeneralSecurityException {

		String imgName = "C:\\Users\\bjlee\\Desktop\\vision data\\bjlee\\20140317_160023.jpg";

		DetectLandmark app = new DetectLandmark(getVisionService());
		List<EntityAnnotation> landmarks = app.identifyLandmark(imgName,
				MAX_RESULTS);
		System.out.printf("Found %d landmark%s\n", landmarks.size(),
				landmarks.size() == 1 ? "" : "s");
		for (EntityAnnotation annotation : landmarks) {
			System.out.printf("\t%s\n", annotation.getDescription());
		}
	}

	// [END run_application]

	// [START authenticate]
	/**
	 * Connects to the Vision API using Application Default Credentials.
	 */
	public static Vision getVisionService() throws IOException,
			GeneralSecurityException {

		GoogleCredential credential = GoogleCredential.getApplicationDefault()
				.createScoped(VisionScopes.all());

		JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

		return new Vision.Builder(GoogleNetHttpTransport.newTrustedTransport(),
				jsonFactory, credential).setApplicationName(APPLICATION_NAME)
				.build();
	}

	// [END authenticate]

	// [START detect_gcs_object]
	private final Vision vision;

	/**
	 * Constructs a {@link DetectLandmark} which connects to the Vision API.
	 */
	public DetectLandmark(Vision vision) {
		this.vision = vision;
	}

	/**
	 * Gets up to {@code maxResults} landmarks for an image stored at
	 * {@code url}.
	 */
	public List<EntityAnnotation> identifyLandmark(String uri, int maxResults)
			throws IOException {

		AnnotateImageRequest request = new AnnotateImageRequest().setImage(
				new Image().encodeContent(getImageString(uri))).setFeatures(
				ImmutableList.of(new Feature().setType("LANDMARK_DETECTION")
						.setMaxResults(maxResults)));

		Vision.Images.Annotate annotate = vision.images().annotate(
				new BatchAnnotateImagesRequest().setRequests(ImmutableList
						.of(request)));

		BatchAnnotateImagesResponse batchResponse = annotate.execute();
		assert batchResponse.getResponses().size() == 1;
		AnnotateImageResponse response = batchResponse.getResponses().get(0);
		if (response.getLandmarkAnnotations() == null) {
			throw new IOException(response.getError() != null ? response
					.getError().getMessage()
					: "Unknown error getting image annotations");
		}
		return response.getLandmarkAnnotations();
	}

	// [END detect_gcs_object]

	public byte[] getImageString(String imgPath) throws IOException {

		byte[] imageBytes = null;

		BufferedImage img = ImageIO.read(new File(imgPath));
		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		try {
			ImageIO.write(img, "JPEG", bos);
			imageBytes = bos.toByteArray();

			bos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return imageBytes;
	}
}
