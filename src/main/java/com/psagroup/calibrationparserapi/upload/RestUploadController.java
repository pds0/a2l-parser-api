package com.psagroup.calibrationparserapi.upload;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.io.Files;
import com.psagroup.calibrationparserapi.service.ParserService;
import io.swagger.annotations.ApiParam;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


@RestController
public class RestUploadController {

    private final Logger logger = LoggerFactory.getLogger(RestUploadController.class);

    @Autowired
    private ParserService parserService;


    @GetMapping("/greetings")
    public ResponseEntity<?> greetings() {
        return new ResponseEntity<>("Hello there !", HttpStatus.OK);
    }


    /**
     * @param a2lFile
     * @param recordFile
     * @return
     * @throws IOException
     * @throws JSONException
     */
    @CrossOrigin
    @PostMapping(path = "/parse/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadFiles(@ApiParam(value = "The .a2l file containing the characteristcs", required = true) @RequestParam("a2lFile") MultipartFile a2lFile,
                                         @ApiParam(value = "The .hex or .ulp file containing the record for the characteristics data", required = true) @RequestParam("recordFile") MultipartFile recordFile) throws IOException, JSONException {

        if (a2lFile.isEmpty() || recordFile.isEmpty()) {
            return new ResponseEntity<>("Two files are required", HttpStatus.BAD_REQUEST);
        }


        ObjectMapper mapper = new ObjectMapper();
        ObjectNode tmpPath = mapper.createObjectNode();


        String dirName = saveUploadedFiles(Arrays.asList(a2lFile, recordFile));
        tmpPath.put("path", dirName);
        logger.info("Files put in \"{}\"", dirName);
        return new ResponseEntity<>(tmpPath, HttpStatus.OK);
    }


    /**
     * @param info
     * @return
     * @throws JSONException
     * @throws IOException
     */
    @CrossOrigin
    @PostMapping(path = "/parse")
    public ResponseEntity<?> launchParse(@RequestBody ConfigInfo info)
            throws JSONException, IOException {

        if (info.isValid()) {

            // start
            long lStartTime = System.nanoTime();

            logger.info(info.toString());


            File folder = new File(info.getFilesPath());

            if (!folder.exists()) {
                return new ResponseEntity<>("Oups! Please make sure that the filesPath is correct!",
                        HttpStatus.BAD_REQUEST);
            }
            // Look for the two files to parse in the folder created
            File a2lFile = Arrays.asList(folder.listFiles((dir, name) -> name.endsWith("a2l"))).get(0);
            File recordFile = Arrays.asList(folder.listFiles((dir, name) -> name.endsWith("hex") || name.endsWith("ulp"))).get(0);

            // Parsing the Record file & the a2l file in parallel
            ExecutorService exec =
                    Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            try {
                if (recordFile.getName().endsWith("hex")) {
                    exec.submit(() -> parserService.parseHexFile(recordFile));
                } else if (recordFile.getName().endsWith("ulp")) {
                    exec.submit(() -> parserService.parseUlpFile(recordFile));
                }
                exec.submit(() -> parserService.parseA2LFile(a2lFile));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                exec.shutdown();
            }

            try {
                exec.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            } catch (Exception e) {
                e.printStackTrace();
            }
            parserService.assignValues(info.getLabels(), parserService.getRecordData());


            // Delete parsed files
            removeUploadedFiles(info.getFilesPath());

            // end
            long lEndTime = System.nanoTime();

            // time elapsed
            long output = lEndTime - lStartTime;
            logger.info("Total parsing time in miliseconds: " + output / 1000000);

            return new ResponseEntity<>(parserService.vh.filteredCaracList, HttpStatus.OK);
        }

        return new ResponseEntity<>("Oups! Something went wrong!", HttpStatus.BAD_REQUEST);
    }

    /**
     * Saves the uploaded files
     *
     * @param files
     * @return
     * @throws IOException
     */
    private String saveUploadedFiles(List<MultipartFile> files) throws IOException {

        File dir = new File("temp" + File.separator + UUID.randomUUID().toString());

        if (dir.mkdirs()) {
            for (MultipartFile f : files) {
                Path path = Paths.get(dir.getAbsolutePath() + File.separator + f.getOriginalFilename());
                Files.write(f.getBytes(), path.toFile());
            }

            return dir.getPath();
        }
        return null;

    }


    /**
     * Removes files after parsing
     *
     * @param path
     * @throws IOException
     */
    private void removeUploadedFiles(String path) throws IOException {
        FileUtils.deleteDirectory(new File(path));
    }


}
