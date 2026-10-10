package com.rescript.plugin.util;

import java.nio.file.Files;
import java.nio.file.Path;

/** A portable subprocess fixture for process lifecycle and pipe regression tests. */
public final class ProcessFixture {
    /** Builds the argument list used by the subprocess tests. */
    public static String[] command(String... args) {
        java.util.List<String> command = new java.util.ArrayList<>();
        command.add(Path.of(System.getProperty("java.home"), "bin", "java").toString());
        command.add("-cp");
        try {
            java.net.URL resource = ProcessFixture.class.getResource("ProcessFixture.class");
            if (resource == null) throw new IllegalStateException("Fixture bytecode not found");
            Path root = Path.of(resource.toURI());
            // Remove the file name and the four package directories.
            for (int i = 0; i < 5; i++) root = root.getParent();
            command.add(root.toString());
        } catch (java.net.URISyntaxException e) {
            throw new IllegalStateException(e);
        }
        command.add(ProcessFixture.class.getName());
        command.addAll(java.util.List.of(args));
        return command.toArray(String[]::new);
    }

    public static void main(String[] args) throws Exception {
        switch (args[0]) {
            case "sleep":
                Thread.sleep(60_000);
                break;
            case "echo":
                System.out.write(System.in.readAllBytes());
                System.err.print("diagnostic");
                break;
            case "flood":
                byte[] block = new byte[8192];
                java.util.Arrays.fill(block, (byte) 'x');
                for (int i = 0; i < 128; i++) {
                    System.out.write(block);
                    System.err.write(block);
                }
                break;
            case "child":
            case "child-exit":
            case "child-immediate-exit":
                Process child = new ProcessBuilder(
                    Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                    "-cp", System.getProperty("java.class.path"), ProcessFixture.class.getName(), "sleep"
                ).inheritIO().start();
                Files.writeString(Path.of(args[1]), Long.toString(child.pid()));
                if (args[0].equals("child")) Thread.sleep(60_000);
                if (args[0].equals("child-exit")) Thread.sleep(200);
                break;
            case "exit":
                System.err.print("bad input");
                System.exit(42);
                break;
            default:
                throw new IllegalArgumentException("Unknown fixture mode");
        }
    }
}
