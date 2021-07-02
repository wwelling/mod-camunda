package org.folio.rest.delegate;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.model.bpmn.instance.FlowElement;
import org.folio.rest.workflow.model.CompressFileTask;
import org.h2.util.IOUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
public class CompressFileDelegate extends AbstractWorkflowIODelegate {

  private static final String ENCODING = "UTF8";
  private static final int BLOCK_SIZE = 8192;

  private static final String EXT_BZIP2 = ".bz2";
  private static final String EXT_GZIP = ".gz";
  private static final String EXT_TAR = ".tar";

  private Expression source;

  private Expression destination;

  private Expression format;

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    long startTime = System.nanoTime();
    FlowElement bpmnModelElement = execution.getBpmnModelElementInstance();
    String delegateName = bpmnModelElement.getName();

    logger.info("{} started", delegateName);

    String sourcePath = this.source.getValue(execution).toString();
    String destinationPath = this.destination.getValue(execution).toString();
    String formatType = this.format.getValue(execution).toString().toLowerCase();
    String extension = "";
    File sourceFile = new File(sourcePath);
    File destinationFile = new File(destinationPath);

    // see: https://commons.apache.org/proper/commons-compress/limitations.html
    switch (formatType) {
      case CompressorStreamFactory.BZIP2:
        extension = EXT_BZIP2;
        break;

      case CompressorStreamFactory.GZIP:
        extension = EXT_GZIP;
        break;

      default:
        logger.info("{} is not a supported format", formatType);
        formatType = null;
        break;
    }

    if (sourceFile.exists()) {
      if (!sourceFile.canRead()) {
        logger.info("{} could not be read", sourceFile);
        formatType = null;
      }
    } else {
      logger.info("{} does not exist", sourceFile);
      formatType = null;
    }

    if (formatType != null) {
      if (destinationFile.isDirectory()) {
        if (!destinationPath.endsWith(File.separator)) {
          destinationPath += File.separator;
        }

        if (sourceFile.isDirectory()) {
          destinationFile = new File(destinationPath + sourceFile.getName() + EXT_TAR + extension);
        } else {
          destinationFile = new File(destinationPath + sourceFile.getName() + extension);
        }
      }

      if (sourceFile.isDirectory()) {
        FileOutputStream outputFile = new FileOutputStream(destinationFile);
        BufferedOutputStream output = new BufferedOutputStream(outputFile);

        CompressorOutputStream compress = new CompressorStreamFactory()
          .createCompressorOutputStream(formatType, output);

        TarArchiveOutputStream tar = new TarArchiveOutputStream(compress, BLOCK_SIZE, ENCODING);

        tar.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_POSIX);
        tar.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);

        try {
          addPaths(sourcePath, "", tar);

          tar.finish();
        } finally {
          tar.close();
          compress.close();
          outputFile.close();
        }
      }
      else {

        try (
          FileInputStream inputFile = new FileInputStream(sourceFile);
          BufferedInputStream input = new BufferedInputStream(inputFile);
          FileOutputStream outputFile = new FileOutputStream(destinationFile);
          BufferedOutputStream output = new BufferedOutputStream(outputFile);
          CompressorOutputStream compress = new CompressorStreamFactory()
            .createCompressorOutputStream(formatType, output);
          ) {
            IOUtils.copy(input, compress);
        }
      }

      logger.info("{} written to {} as {}", source, destination, format);
    }

    long endTime = System.nanoTime();
    logger.info("{} finished in {} milliseconds", delegateName, (endTime - startTime) / (double) 1000000);
  }

  public void setSource(Expression path) {
    this.source = path;
  }

  public void setDestination(Expression path) {
    this.destination = path;
  }

  public void setFormat(Expression op) {
    this.format = op;
  }

  @Override
  public Class<?> fromTask() {
    return CompressFileTask.class;
  }

  private void addPaths(String path, String parentPath, TarArchiveOutputStream tar) throws IOException {
    File file = new File(path);

    setupEntryHeader(path, parentPath, tar, file);

    if (file.isDirectory()) {
      tar.closeArchiveEntry();

      for (File f : file.listFiles()) {
        addPaths(f.getAbsolutePath(), parentPath + file.getName() + File.separator, tar);
      }
    } else {
      try (BufferedInputStream input = new BufferedInputStream(new FileInputStream(file))) {
        IOUtils.copy(input, tar);
      }

      tar.closeArchiveEntry();
    }
  }

  private void setupEntryHeader(String path, String parentPath, TarArchiveOutputStream tar, File file) throws IOException {
    Path filePath = Path.of(path);
    TarArchiveEntry entry = new TarArchiveEntry(file, parentPath + file.getName());

    if (file.isFile()) {
      entry.setSize(Files.size(Paths.get(path)));
    }

    entry.setModTime(Files.getLastModifiedTime(filePath).toMillis());

    tar.putArchiveEntry(entry);
  }

}
