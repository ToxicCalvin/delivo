package com.delivo.ai.envelope;

import com.delivo.entity.AddressBook;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class AddressListFormatter {

    
    public String formatForText(List<AddressBook> addresses, String language) {
        if (addresses == null || addresses.isEmpty()) return "";

        Labels labels = labelsFor(language);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < addresses.size(); i++) {
            AddressBook a = addresses.get(i);
            if (i > 0) sb.append("\n");
            sb.append(i + 1).append(". ")
                    .append(labels.name).append(": ").append(safe(a.getConsignee())).append(", ")
                    .append(labels.phone).append(": ").append(safe(a.getPhone())).append(", ")
                    .append(labels.address).append(": ").append(safe(a.getDetail()));
        }
        return sb.toString();
    }

    public String quickReplyLabel(int oneBasedIndex, String language) {
        switch (language) {
            case "zh": return "使用地址 " + oneBasedIndex;
            case "hu": return oneBasedIndex + ". cím használata";
            case "ko": return "주소 " + oneBasedIndex + " 사용";
            default:   return "Use Address " + oneBasedIndex;
        }
    }

    
    public String newAddressQuickReply(String language) {
        switch (language) {
            case "zh": return "新建地址";
            case "hu": return "Új cím hozzáadása";
            case "ko": return "새 주소 추가";
            default:   return "Add a New Address";
        }
    }

    private Labels labelsFor(String language) {
        switch (language) {
            case "zh": return new Labels("姓名", "电话", "地址");
            case "hu": return new Labels("Név", "Telefon", "Cím");
            case "ko": return new Labels("이름", "전화", "주소");
            default:   return new Labels("Name", "Phone", "Address");
        }
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private static class Labels {
        final String name, phone, address;
        Labels(String name, String phone, String address) {
            this.name = name;
            this.phone = phone;
            this.address = address;
        }
    }
}
