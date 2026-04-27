package com.luiscampillo.hotelnova.report;

import com.luiscampillo.hotelnova.config.AppConfig;
import com.luiscampillo.hotelnova.model.entity.Reservation;
import com.luiscampillo.hotelnova.model.entity.Room;
import com.luiscampillo.hotelnova.util.AppLogger;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Logger;

/**
 * Writes CSV files into the directory configured by reports.directory.
 *
 * Two reports are required by the rubric:
 *   - room listing
 *   - active reservations
 *
 * String values are escaped per RFC 4180: any field containing a comma,
 * quote or newline is double-quoted and embedded quotes are doubled.
 */
public class CsvExporter {

    private static final Logger LOG = AppLogger.getLogger(CsvExporter.class);
    private static final DateTimeFormatter TS_FMT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final Path reportsDir;

    public CsvExporter() {
        this.reportsDir = Paths.get(AppConfig.getInstance().getReportsDirectory());
    }

    public Path exportRooms(List<Room> rooms) {
        Path file = nextFile("rooms");
        String[] header = { "id", "room_number", "type", "price_per_night", "status" };

        try (BufferedWriter w = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            writeRow(w, header);
            for (Room r : rooms) {
                writeRow(w,
                        String.valueOf(r.getId()),
                        r.getRoomNumber(),
                        r.getType().name(),
                        r.getPricePerNight().toPlainString(),
                        r.getStatus().name());
            }
            LOG.info("Rooms CSV exported: " + file);
            return file;
        } catch (IOException e) {
            throw new RuntimeException("Failed to export rooms CSV", e);
        }
    }

    public Path exportActiveReservations(List<Reservation> reservations) {
        Path file = nextFile("active_reservations");
        String[] header = {
                "id", "guest_id", "room_id", "user_id",
                "check_in_date", "check_out_date",
                "actual_check_in", "status", "total_cost"
        };

        try (BufferedWriter w = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            writeRow(w, header);
            for (Reservation r : reservations) {
                writeRow(w,
                        String.valueOf(r.getId()),
                        String.valueOf(r.getGuestId()),
                        String.valueOf(r.getRoomId()),
                        String.valueOf(r.getUserId()),
                        String.valueOf(r.getCheckInDate()),
                        String.valueOf(r.getCheckOutDate()),
                        String.valueOf(r.getActualCheckIn()),
                        r.getStatus().name(),
                        r.getTotalCost().toPlainString());
            }
            LOG.info("Active reservations CSV exported: " + file);
            return file;
        } catch (IOException e) {
            throw new RuntimeException("Failed to export active reservations CSV", e);
        }
    }

    // ---- Internals ----------------------------------------------------

    private Path nextFile(String prefix) {
        try {
            Files.createDirectories(reportsDir);
        } catch (IOException e) {
            throw new RuntimeException("Cannot create reports directory: " + reportsDir, e);
        }
        String name = prefix + "_" + LocalDateTime.now().format(TS_FMT) + ".csv";
        return reportsDir.resolve(name);
    }

    private void writeRow(BufferedWriter w, String... fields) throws IOException {
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) line.append(',');
            line.append(escape(fields[i]));
        }
        w.write(line.toString());
        w.newLine();
    }

    /** RFC 4180 escaping: quote fields with comma/quote/newline, double internal quotes. */
    private String escape(String value) {
        if (value == null) return "";
        boolean needsQuoting = value.contains(",") || value.contains("\"")
                || value.contains("\n") || value.contains("\r");
        if (!needsQuoting) return value;
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
