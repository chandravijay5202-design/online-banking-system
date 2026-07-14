package com.chandravijay.banking.service.impl;

import com.chandravijay.banking.entity.Account;
import com.chandravijay.banking.entity.Transaction;
import com.chandravijay.banking.exception.ResourceNotFoundException;
import com.chandravijay.banking.repository.AccountRepository;
import com.chandravijay.banking.repository.TransactionRepository;
import com.chandravijay.banking.service.StatementService;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatementServiceImpl implements StatementService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    @Override
    @Transactional(readOnly = true)
    public byte[] generateStatementPdf(Long accountId, String username, boolean isAdmin) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + accountId));

        if (!isAdmin && !account.getOwner().getUsername().equals(username)) {
            throw new AccessDeniedException("You do not own this account");
        }

        List<Transaction> transactions = transactionRepository
                .findByFromAccountIdOrToAccountIdOrderByTimestampDesc(accountId, accountId);

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4, 40, 40, 50, 40);
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new Color(30, 41, 59));
            Font headingFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.WHITE);
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.DARK_GRAY);
            Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.GRAY);

            document.add(new Paragraph("Chandravijay Bank", titleFont));
            document.add(new Paragraph("Account Statement", FontFactory.getFont(FontFactory.HELVETICA, 12, Color.GRAY)));
            document.add(spacer());

            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            addInfoRow(infoTable, "Account Number", account.getAccountNumber(), bodyFont);
            addInfoRow(infoTable, "Account Holder", account.getOwner().getFullName(), bodyFont);
            addInfoRow(infoTable, "Account Type", account.getAccountType().toString(), bodyFont);
            addInfoRow(infoTable, "Branch", account.getBranchName() + ", " + account.getCity(), bodyFont);
            addInfoRow(infoTable, "IFSC Code", account.getIfscCode(), bodyFont);
            addInfoRow(infoTable, "Current Balance", "Rs. " + account.getBalance(), bodyFont);
            document.add(infoTable);
            document.add(spacer());

            PdfPTable table = new PdfPTable(new float[]{2.3f, 1.2f, 3f, 1.5f, 1.3f});
            table.setWidthPercentage(100);

            String[] headers = {"Date", "Type", "Description", "Amount (Rs.)", "Mode"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Paragraph(h, headingFont));
                cell.setBackgroundColor(new Color(30, 41, 59));
                cell.setPadding(6);
                table.addCell(cell);
            }

            if (transactions.isEmpty()) {
                PdfPCell empty = new PdfPCell(new Paragraph("No transactions found for this account.", bodyFont));
                empty.setColspan(5);
                empty.setPadding(10);
                table.addCell(empty);
            }

            for (Transaction t : transactions) {
                boolean isCredit = t.getToAccount() != null && t.getToAccount().getId().equals(accountId);
                String sign = isCredit ? "+ " : "- ";
                String amountText = t.getType().name().equals("DEPOSIT") || (t.getType().name().equals("TRANSFER") && isCredit)
                        ? sign + t.getAmount()
                        : "- " + t.getAmount();
                if (t.getType().name().equals("WITHDRAWAL")) {
                    amountText = "- " + t.getAmount();
                }

                addCell(table, t.getTimestamp().format(DATE_FORMAT), bodyFont);
                addCell(table, t.getType().toString(), bodyFont);
                addCell(table, t.getDescription() != null ? t.getDescription() : t.getReference(), bodyFont);
                addCell(table, amountText, bodyFont);
                addCell(table, t.getMode() != null ? t.getMode().toString() : "-", bodyFont);
            }

            document.add(table);
            document.add(spacer());

            Paragraph footer = new Paragraph(
                    "This is a system-generated statement for demonstration purposes and is not a legally valid bank document.",
                    smallFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate statement PDF: " + e.getMessage(), e);
        }
    }

    private void addInfoRow(PdfPTable table, String label, String value, Font font) {
        PdfPCell labelCell = new PdfPCell(new Paragraph(label, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.DARK_GRAY)));
        labelCell.setBorder(0);
        labelCell.setPadding(3);
        PdfPCell valueCell = new PdfPCell(new Paragraph(value, font));
        valueCell.setBorder(0);
        valueCell.setPadding(3);
        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private void addCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Paragraph(text, font));
        cell.setPadding(5);
        table.addCell(cell);
    }

    private Paragraph spacer() {
        return new Paragraph(" ");
    }
}
