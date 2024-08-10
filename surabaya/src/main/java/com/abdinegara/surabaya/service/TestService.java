package com.abdinegara.surabaya.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class TestService {

    public boolean cheksimbol( String input){
        System.out.println("input" + input);
        if(input == null || input.isEmpty() || input.isBlank()) return false;

        String[] pembuka = {"{","[","("};
        String[] penutup = {"}","]",")"};
        String[] simbol = {"{}","[]","()"};

        input = input.replaceAll("[a-zA-Z0-9&&[^\\[\\](){}]]", "");
        boolean isGanjil = input.length() % 2 == 1;
        System.out.println("gajil" + isGanjil);
        if(isGanjil) return false;

        char[] inputChar = input.toCharArray();
        int ipsize = inputChar.length + 1;
        int indexTerkahir = -1;
        for(int i=0; i< inputChar.length; i++){
            List<String> simbolget = new ArrayList<String>();
            String ipString = String.valueOf(inputChar[i]);
            boolean isPenutup = Arrays.stream(penutup).anyMatch(e -> {
                e.equals(ipString);
                simbolget.add(e);
                return e.equals(ipString);
            });

            if(isPenutup){
                String simbolGetData = simbolget.get(0);
                char[] simbolGetDataChar = simbolGetData.toCharArray();
                int indexlama = indexTerkahir == -1 ? i-1 : indexTerkahir -1;
                String ipStringPembuka = String.valueOf(inputChar[ indexlama]);
                boolean isPembuka = Arrays.stream(penutup).anyMatch(e -> {
                    e.equals(ipStringPembuka);
                    return e.equals(ipString);
                });

                if (Arrays.asList(simbolGetDataChar).contains(inputChar[indexlama])
                 && isPembuka) {
                    ipsize = ipsize - 2;
                    indexTerkahir = indexlama;
                }
            }
        }
        System.out.println("indexTerkahir" + indexTerkahir);
        if(ipsize == 0){
            return true;
        }

        return false;
    }
}
