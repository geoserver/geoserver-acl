/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.app;

import org.geoserver.acl.generateddl.GenerateDDL;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;

@SpringBootApplication
public class AccesControlListApplication {

    private static final String ENCODEPASSWORD = "encodepassword";
    private static final String GENERATEDDL = "generateddl";

    public static void main(String... args) throws Exception {
        List<String> arglist = Arrays.asList(args);

        if (arglist.contains(ENCODEPASSWORD)) {
            System.exit(encodePassword(arglist));
        } else if (arglist.contains(GENERATEDDL)) {
            GenerateDDL.main(args);
        }

        try {
            SpringApplication.run(AccesControlListApplication.class, args);
        } catch (RuntimeException e) {
            System.exit(-1);
        }
    }

    private static int encodePassword(List<String> arglist) {
        int pwdIndex = 1 + arglist.indexOf(ENCODEPASSWORD);
        if (arglist.size() < pwdIndex) {
            System.err.println("Usage: encodepassword <password>");
            return -1;
        }
        String pwd = arglist.get(pwdIndex);
        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        String encoded = encoder.encode(pwd);
        System.out.println(encoded);
        return 0;
    }
}
