package org.aksw.sparqlmap.web.negotiation;

import java.io.IOException;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

public class SparqlRdfXmlResponse extends AbstractHttpMessageConverter<Object> {

	@Override
	protected Object readInternal(Class arg0, HttpInputMessage arg1)
			throws IOException, HttpMessageNotReadableException {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public boolean canRead(Class<?> clazz, MediaType mediaType) {
		return false;
	}
	
	
	@Override
	public boolean canWrite(Class<?> clazz, MediaType mediaType) {
		// TODO Auto-generated method stub
		return false;
	}
	

	@Override
	protected boolean supports(Class arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void writeInternal(Object arg0, HttpOutputMessage arg1)
			throws IOException, HttpMessageNotWritableException {
		
		
		
	}

}
