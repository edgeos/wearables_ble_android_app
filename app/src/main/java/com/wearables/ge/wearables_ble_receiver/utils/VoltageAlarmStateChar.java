package com.wearables.ge.wearables_ble_receiver.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VoltageAlarmStateChar {

    private Boolean overall_alarm;
    private Boolean ch1_alarm;
    private Boolean ch2_alarm;
    private Boolean ch3_alarm;
    private int num_fft_bins;
    private int fft_bin_size;
    private List<Integer> ch1_fft_results;
    private List<Integer> ch2_fft_results;
    private List<Integer> ch3_fft_results;

    public VoltageAlarmStateChar (String hexString){
        List<String> hexSplit = Arrays.asList(hexString.split("\\s+"));
        this.overall_alarm = !hexSplit.get(0).equals("00");

        this.ch1_alarm = !hexSplit.get(1).equals("00");

        this.ch2_alarm = !hexSplit.get(2).equals("00");

        this.ch3_alarm = !hexSplit.get(3).equals("00");

        this.num_fft_bins = Integer.parseInt(hexSplit.get(4), 16);
        this.fft_bin_size = Integer.parseInt(hexSplit.get(5), 16);

        List<Integer> ch1 = new ArrayList<>();
        for(int i = 6; i < (this.num_fft_bins + 6); i ++ ){
            ch1.add(Integer.parseInt(hexSplit.get(i), 16));
        }
        this.ch1_fft_results = ch1;

        List<Integer> ch2 = new ArrayList<>();
        for(int i = (6 + this.num_fft_bins); i < ((this.num_fft_bins*2) + 6); i ++ ){
            ch2.add(Integer.parseInt(hexSplit.get(i), 16));
        }
        this.ch2_fft_results = ch2;

        List<Integer> ch3 = new ArrayList<>();
        for(int i = (6 + (this.num_fft_bins*2)); i < (hexSplit.size()); i ++ ){
            ch3.add(Integer.parseInt(hexSplit.get(i), 16));
        }
        this.ch3_fft_results = ch3;
    }

    public int getNum_fft_bins() {
        return num_fft_bins;
    }

    public int getFft_bin_size() {
        return fft_bin_size;
    }

    public List<Integer> getCh1_fft_results() {
        return ch1_fft_results;
    }

    public List<Integer> getCh2_fft_results() {
        return ch2_fft_results;
    }

    public List<Integer> getCh3_fft_results() {
        return ch3_fft_results;
    }

}
