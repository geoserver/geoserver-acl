/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.generateddl;

import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@SpringBootApplication
@Slf4j
public class GenerateDDL implements CommandLineRunner {

    private static File tmpfile;

    private static int exitCode = 0;

    public static void main(String... args) throws IOException {
        tmpfile = File.createTempFile("acl-create", ".sql");
        log.debug("target file: {}", tmpfile.getAbsolutePath());
        System.setProperty("scripts.create-target", tmpfile.getAbsolutePath());

        ConfigurableEnvironment environment = new StandardEnvironment();
        environment.addActiveProfile("ddl");

        SpringApplication cliApp = new SpringApplication(GenerateDDL.class);
        cliApp.setEnvironment(environment);
        cliApp.run(args);
        System.exit(exitCode);
    }

    @Override
    @SuppressWarnings("java:S106")
    public void run(String... args) throws Exception {
        try (FileInputStream in = new FileInputStream(tmpfile)) {
            FileCopyUtils.copy(in, System.out);
            System.out.flush();
        } catch (Exception e) {
            exitCode = -1;
            throw e;
        } finally {
            tmpfile.delete();
        }
    }
}
