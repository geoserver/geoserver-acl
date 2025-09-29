/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.api.server.support;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.servlet.FilterChain;
import javax.servlet.ReadListener;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * @since 1.0
 */
public class RequestBodyBufferingServletFilter implements javax.servlet.Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        if ("PATCH".equalsIgnoreCase(req.getMethod())) {
            request = new RequestBodyBufferingServletRequest(req);
        }
        chain.doFilter(request, response);
    }

    public static class RequestBodyBufferingServletRequest extends HttpServletRequestWrapper {
        protected byte[] buffer;
        protected ServletInputStream myStream;

        public RequestBodyBufferingServletRequest(HttpServletRequest request) {
            super(request);
        }

        public @Override ServletInputStream getInputStream() throws IOException {
            final ByteArrayInputStream buffer = getBuffer();
            return new ServletInputStream() {

                public @Override int read() throws IOException {
                    return buffer.read();
                }

                public @Override void setReadListener(ReadListener readListener) {}

                public @Override boolean isReady() {
                    return true;
                }

                public @Override boolean isFinished() {
                    return buffer.available() == 0;
                }
            };
        }

        public @Override BufferedReader getReader() throws IOException {
            return new BufferedReader(new InputStreamReader(getBuffer()));
        }

        private ByteArrayInputStream getBuffer() throws IOException {
            if (this.buffer == null) {
                this.buffer = readFully(super.getInputStream());
            }
            return new ByteArrayInputStream(this.buffer);
        }

        private byte[] readFully(ServletInputStream inputStream) throws IOException {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int c;
            while (-1 != (c = inputStream.read())) {
                out.write(c);
            }
            return out.toByteArray();
        }
    }
}
