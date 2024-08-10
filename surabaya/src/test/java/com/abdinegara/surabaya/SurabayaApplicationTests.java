package com.abdinegara.surabaya;

import com.abdinegara.surabaya.service.TestService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import static org.mockito.Mockito.when;

@SpringBootTest
class SurabayaApplicationTests {

	@Mock
	TestService testService;

	@Test
	void contextLoads() {
		System.out.println(checkdata("data"));
	}

	static boolean checkdata(String data){

		data = data.replaceAll("\\s","");
		data.length();
		System.out.println(data.length());

		char[] charArray1 = data.toCharArray();
		System.out.println(charArray1);
		System.out.println(charArray1[1]);
		boolean cek = Arrays.equals(charArray1,charArray1);
		System.out.println(cek);
		return false;
	}

	@Test
	void testHitung(){
		System.out.println(isBalanced("([]))((8){}(())()()"));
	}

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
		int ipsize = inputChar.length ;
		System.out.println("ipsize" + ipsize);
		int indexTerkahir = -1;
		for(int i=0; i< inputChar.length; i++){
			List<String> simbolget = new ArrayList<String>();
			String ipString = String.valueOf(inputChar[i]);
			boolean isPenutup = Arrays.stream(penutup).anyMatch(e -> {
				e.equals(ipString);
//				simbolget.add(e);
				return e.equals(ipString);
			});
			System.out.println(i + "semua" + ipString);
			if(isPenutup){
				System.out.println(i + "isPenutup" + ipString);



				Arrays.stream(simbol).forEach(e->{
					String data = e;
					if(data.contains(ipString)){
						simbolget.add(e);
					}

				});

				String simbolGetData = simbolget.get(0);
				System.out.println(i + "simbolGetData" + simbolGetData);
				String [] simbolGetDataList = simbolGetData.split("");
				char[] simbolGetDataChar = simbolGetData.toCharArray();
				int indexlama = indexTerkahir == -1 ? i-1 : indexTerkahir -1;

				String ipStringPembuka = String.valueOf(inputChar[ indexlama]);
				System.out.println(indexlama + "ipStringPembuka" + ipStringPembuka);
				boolean isPembuka = Arrays.stream(pembuka).anyMatch(e -> {

					return e.equals(ipStringPembuka);
				});
				System.out.println(indexlama + "isPembuka" + isPembuka);

				System.out.println(i + "simbolGetDataChar" + simbolGetData);
				boolean containSymbol = false;

				for(String data : simbolGetDataList){
					System.out.println(i + "data" + data + " "+  ipStringPembuka);
					if(ipStringPembuka.equals(data)) containSymbol = true;
				}

				System.out.println("containSymbol" + containSymbol);
				if (containSymbol
						&& isPembuka) {

					System.out.println("masuk pembuka" + ipStringPembuka);
					ipsize = ipsize - 2;
					indexTerkahir = indexlama;
				}
			}
		}
		System.out.println("indexTerkahir" + ipsize);
		if(ipsize == 0){
			return true;
		}

		return false;
	}

	public static boolean isBalanced(String input) {
		Stack<Character> stack = new Stack<>();

		for (int i = 0; i < input.length(); i++) {
			char current = input.charAt(i);

			if (current == '(' || current == '{' || current == '[') {
				stack.push(current);
			} else if (current == ')' || current == '}' || current == ']') {
				if (stack.isEmpty()) {
					return false; // Closing bracket with no matching opening bracket
				}

				char top = stack.pop();

				if ((current == ')' && top != '(') ||
						(current == '}' && top != '{') ||
						(current == ']' && top != '[')) {
					return false; // Mismatched brackets
				}
			}
		}

		return stack.isEmpty(); // If stack is empty, all brackets were matched
	}
}
