package de.u808.dev.dnstest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class DoHResponseTest {
	
	Gson gson = new GsonBuilder().setPrettyPrinting().create();
	
	private static Logger LOG = LoggerFactory.getLogger(DoHResponseTest.class);

	@Test
	public void testSuccessResponse() throws FileNotFoundException {		
		DoHResponse response = readFile("src/test/resources/success-response.json");		
		assertNotNull(response);		
		assertTrue(successStatusExpected(response, true));
	}
	
	@Test
	public void testFailureResponse() throws FileNotFoundException {		
		DoHResponse response = readFile("src/test/resources/failure-response.json");		
		assertNotNull(response);
		assertTrue(successStatusExpected(response, false));
	}
	
	@Test
	public void testSpfSuccessResponse()throws FileNotFoundException {	
		DoHResponse response = readFile("src/test/resources/success-spf-response.json");		
		assertNotNull(response);		
		assertTrue(successStatusExpected(response, true));
	}
	
	@Test
	public void testTxtSuccessResponse()throws FileNotFoundException {	
		DoHResponse response = readFile("src/test/resources/success-txt-response.json");		
		assertNotNull(response);		
		assertTrue(successStatusExpected(response, true));
	}
	
	boolean successStatusExpected(DoHResponse response, boolean success) {
		if(success) {
			return response.getStatus() == 0;
		} else {
			return response.getStatus() > 0;
		}
	}
	
	protected DoHResponse readFile(final String file) {
		DoHResponse response = null;
		try (Reader reader = new FileReader(file)) {
			response = gson.fromJson(reader, DoHResponse.class);					
		} catch (IOException e) {
            LOG.error("Failed to read file" + file, e);
        }
		return response;
	}
}
