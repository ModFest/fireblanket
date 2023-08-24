package net.modfest.fireblanket;

public class TestMagicDivision {
	public static void main(String[] args) {
		for (int i = 0; i < 4096; i++) {
			int rawIdx = i;

			int oDivRes = rawIdx / 3;
			int oModRes = (rawIdx % 3) * 20;

			int divRes = (rawIdx * 0xAAAB) >>> 17;
			int modRes = -((divRes + (divRes * 2)) - rawIdx);

			assert oDivRes == divRes : "Division results don't match " + oDivRes + " " + divRes;
			assert oModRes == modRes : "Modulo results don't match " + oModRes + " " + modRes;
		}
	}
}
