// 
// Copyright 2015 Naver
// Author : Dongseok Hyun <dustin.hyun@navercorp.com>
//
// This file is part of Usher.
//
// Usher is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// Usher is distrubuted in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with Usher. If not, see <http://www.gnu.org/licenses/>.

// UsherTest.java
//
// Usher test binary.

import org.json.simple.parser.ParseException;

public class UsherTest {

	private static String sampleData = "{\"nodes\":[\"n1\", \"n2\", \"n3\", \"n4\"], \"edges\":[ [\"n1\", \"n2\"], [\"n1\", \"n3\"], [\"n3\", \"n4\"] ],\"presets\":{\"n1\":[100, 200], \"n2\":[300, 400]}}";

	public static void main(String[] args) {

		Usher usher = new Usher();
		usher.setPdfExport();

		try {
			String result = usher.getLayoutData(sampleData);
			System.out.println(result);
		}
		catch (ParseException ex) {
			System.err.println(ex.toString());
		}
	}
}
