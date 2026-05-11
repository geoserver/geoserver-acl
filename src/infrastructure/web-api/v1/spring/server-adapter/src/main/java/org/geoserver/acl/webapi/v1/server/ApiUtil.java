package org.geoserver.acl.webapi.v1.server;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import lombok.experimental.UtilityClass;
import org.springframework.web.context.request.NativeWebRequest;

@UtilityClass
class ApiUtil {
    public static void setExampleResponse(NativeWebRequest req, String contentType, String example) {
        try {
            HttpServletResponse res = req.getNativeResponse(HttpServletResponse.class);
            if (res != null) {
                res.setCharacterEncoding(StandardCharsets.UTF_8);
                res.addHeader("Content-Type", contentType);
                res.getWriter().print(example);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
