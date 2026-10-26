package vn.edu.detai.dto;

import vn.edu.detai.entity.BoMon;

public record BoMonThongKe(BoMon boMon, long soGv, long soDeTai) {
    public boolean xoaDuoc() { return soGv == 0 && soDeTai == 0; }
}
