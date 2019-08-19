package com.sergeybannikov.alfrescogateway.utils;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.internal.Util;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;

import java.io.IOException;
import java.io.InputStream;

public class RequestBodyUtil {

    public static RequestBody create(final MediaType mediaType, final InputStream inputStream, final Long contentLength
    ) {
        return new RequestBody() {
            @Override
            public MediaType contentType() {
                return mediaType;
            }

            @Override
            public long contentLength() {
                if (contentLength != null) {
                    return contentLength;
                }
                try {
                    return inputStream.available();
                } catch (IOException e) {
                    return 0;
                }
            }

            @Override
            public void writeTo(BufferedSink sink) throws IOException {
                Source source = null;
                try {
                    source = Okio.source(inputStream);
                    System.out.println("sink.writeAll(source)");
                    sink.writeAll(source);
                } finally {
                    Util.closeQuietly(source);
                }
            }
        };
    }
}