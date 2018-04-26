package octo;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Download implements RequestHandler<Map<String, Object>, String> {
  @Override
  public String handleRequest(Map<String, Object> event, Context context) {
    final AmazonS3 s3 = AmazonS3ClientBuilder.defaultClient();
    try {
      zip("/var/runtime/lib", "/tmp/internals.zip");
      s3.putObject((String) event.get("bucket"), "internals.zip", new File("/tmp/internals.zip"));
    } catch (IOException e) {
      return e.getMessage();
    }

    return "ok";
  }


  static void zip(String sourceDirPath, String zipFilePath) throws IOException {
    Path zipPath = Files.createFile(Paths.get(zipFilePath));
    try (ZipOutputStream zipStream = new ZipOutputStream(Files.newOutputStream(zipPath))) {
      Path sourcePath = Paths.get(sourceDirPath);
      Files.walk(sourcePath)
          .filter(path -> !Files.isDirectory(path))
          .forEach(path -> {
            ZipEntry zipEntry = new ZipEntry(sourcePath.relativize(path).toString());
            try {
              zipStream.putNextEntry(zipEntry);
              Files.copy(path, zipStream);
              zipStream.closeEntry();
            } catch (IOException e) {
              e.printStackTrace();
            }
          });
    }
  }
}
