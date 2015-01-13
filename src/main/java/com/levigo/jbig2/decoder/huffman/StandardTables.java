/**
 * Copyright (C) 1995-2015 levigo holding gmbh.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.levigo.jbig2.decoder.huffman;

import java.util.ArrayList;
import java.util.List;
import com.levigo.jbig2.*;

public class StandardTables {
	static class StandardTable extends HuffmanTable {
		private StandardTable(int table[][]) {

			List<Code> codeTable = new ArrayList<Code>();

			for (int i = 0; i < table.length; i++) {
				int prefixLength = table[i][0];
				int rangeLength = table[i][1];
				int rangeLow = table[i][2];
				boolean isLowerRange = false;
				if (table[i].length > 3)
					isLowerRange = true;
				codeTable.add(new Code(prefixLength, rangeLength, rangeLow,
						isLowerRange));
			}

			if (JBIG2ImageReader.DEBUG)
				System.out.println(HuffmanTable.codeTableToString(codeTable));

			initTree(codeTable);
		}
	}

	// Fourth Value (999) is used for the LowerRange-line
	private static final int TABLES[][][] = {
	// B1
			{   { 1, 4, 0 }, //
					{ 2, 8, 16 }, //
					{ 3, 16, 272 }, //
					{ 3, 32, 65808 } /* high */
			},
			// B2
			{   { 1, 0, 0 }, //
					{ 2, 0, 1 }, //
					{ 3, 0, 2 }, //
					{ 4, 3, 3 }, //
					{ 5, 6, 11 }, //
					{ 6, 32, 75 }, /* high */
					{ 6, -1, 0 } /* OOB */
			},
			// B3
			{   { 8, 8, -256 }, //
					{ 1, 0, 0 }, //
					{ 2, 0, 1 }, //
					{ 3, 0, 2 }, //
					{ 4, 3, 3 }, //
					{ 5, 6, 11 }, //
					{ 8, 32, Integer.MIN_VALUE, 999 }, /* low */
					{ 7, 32, 75 }, /* high */
					{ 6, -1, 0 } /* OOB */
			},
			// B4
			{   { 1, 0, 1 }, //
					{ 2, 0, 2 }, //
					{ 3, 0, 3 }, //
					{ 4, 3, 4 }, //
					{ 5, 6, 12 }, //
					{ 5, 32, 76 } /* high */
			},
			// B5
			{   { 7, 8, -255 }, //
					{ 1, 0, 1 }, //
					{ 2, 0, 2 }, //
					{ 3, 0, 3 }, //
					{ 4, 3, 4 }, //
					{ 5, 6, 12 }, //
					{ 7, 32, Integer.MIN_VALUE, 999 }, /* low */
					{ 6, 32, 76 } /* high */
			},
			// B6
			{   { 5, 10, -2048 }, //
					{ 4, 9, -1024 }, //
					{ 4, 8, -512 }, //
					{ 4, 7, -256 }, //
					{ 5, 6, -128 }, //
					{ 5, 5, -64 }, //
					{ 4, 5, -32 }, //
					{ 2, 7, 0 }, //
					{ 3, 7, 128 }, //
					{ 3, 8, 256 }, //
					{ 4, 9, 512 }, //
					{ 4, 10, 1024 }, //
					{ 6, 32, Integer.MIN_VALUE, 999 }, /* low */
					{ 6, 32, 2048 } /* high */
			},
			// B7
			{   { 4, 9, -1024 }, //
					{ 3, 8, -512 }, //
					{ 4, 7, -256 }, //
					{ 5, 6, -128 }, //
					{ 5, 5, -64 }, //
					{ 4, 5, -32 }, //
					{ 4, 5, 0 }, //
					{ 5, 5, 32 }, //
					{ 5, 6, 64 }, //
					{ 4, 7, 128 }, //
					{ 3, 8, 256 }, //
					{ 3, 9, 512 }, //
					{ 3, 10, 1024 }, //
					{ 5, 32, Integer.MIN_VALUE, 999 }, /* low */
					{ 5, 32, 2048 } /* high */
			},
			// B8
			{   { 8, 3, -15 }, //
					{ 9, 1, -7 }, //
					{ 8, 1, -5 }, //
					{ 9, 0, -3 }, //
					{ 7, 0, -2 }, //
					{ 4, 0, -1 }, //
					{ 2, 1, 0 }, //
					{ 5, 0, 2 }, //
					{ 6, 0, 3 }, //
					{ 3, 4, 4 }, //
					{ 6, 1, 20 }, //
					{ 4, 4, 22 }, //
					{ 4, 5, 38 }, //
					{ 5, 6, 70 }, //
					{ 5, 7, 134 }, //
					{ 6, 7, 262 }, //
					{ 7, 8, 390 }, //
					{ 6, 10, 646 }, //
					{ 9, 32, Integer.MIN_VALUE, 999 }, /* low */
					{ 9, 32, 1670 }, /* high */
					{ 2, -1, 0 } /* OOB */
			},
			// B9
			{   { 8, 4, -31 }, //
					{ 9, 2, -15 }, //
					{ 8, 2, -11 }, //
					{ 9, 1, -7 }, //
					{ 7, 1, -5 }, //
					{ 4, 1, -3 }, //
					{ 3, 1, -1 }, //
					{ 3, 1, 1 }, //
					{ 5, 1, 3 }, //
					{ 6, 1, 5 }, //
					{ 3, 5, 7 }, //
					{ 6, 2, 39 }, //
					{ 4, 5, 43 }, //
					{ 4, 6, 75 }, //
					{ 5, 7, 139 }, //
					{ 5, 8, 267 }, //
					{ 6, 8, 523 }, //
					{ 7, 9, 779 }, //
					{ 6, 11, 1291 }, //
					{ 9, 32, Integer.MIN_VALUE, 999 }, /* low */
					{ 9, 32, 3339 }, /* high */
					{ 2, -1, 0 } /* OOB */
			},
			// B10
			{   { 7, 4, -21 }, //
					{ 8, 0, -5 }, //
					{ 7, 0, -4 }, //
					{ 5, 0, -3 }, //
					{ 2, 2, -2 }, //
					{ 5, 0, 2 }, //
					{ 6, 0, 3 }, //
					{ 7, 0, 4 }, //
					{ 8, 0, 5 }, //
					{ 2, 6, 6 }, //
					{ 5, 5, 70 }, //
					{ 6, 5, 102 }, //
					{ 6, 6, 134 }, //
					{ 6, 7, 198 }, //
					{ 6, 8, 326 }, //
					{ 6, 9, 582 }, //
					{ 6, 10, 1094 }, //
					{ 7, 11, 2118 }, //
					{ 8, 32, Integer.MIN_VALUE, 999 }, /* low */
					{ 8, 32, 4166 }, /* high */
					{ 2, -1, 0 } /* OOB */
			},
			// B11
			{   { 1, 0, 1 }, //
					{ 2, 1, 2 }, //
					{ 4, 0, 4 }, //
					{ 4, 1, 5 }, //
					{ 5, 1, 7 }, //
					{ 5, 2, 9 }, //
					{ 6, 2, 13 }, //
					{ 7, 2, 17 }, //
					{ 7, 3, 21 }, //
					{ 7, 4, 29 }, //
					{ 7, 5, 45 }, //
					{ 7, 6, 77 }, //
					{ 7, 32, 141 } /* high */
			},
			// B12
			{   { 1, 0, 1 }, //
					{ 2, 0, 2 }, //
					{ 3, 1, 3 }, //
					{ 5, 0, 5 }, //
					{ 5, 1, 6 }, //
					{ 6, 1, 8 }, //
					{ 7, 0, 10 }, //
					{ 7, 1, 11 }, //
					{ 7, 2, 13 }, //
					{ 7, 3, 17 }, //
					{ 7, 4, 25 }, //
					{ 8, 5, 41 }, //
					{ 8, 32, 73 } //
			},
			// B13
			{   { 1, 0, 1 }, //
					{ 3, 0, 2 }, //
					{ 4, 0, 3 }, //
					{ 5, 0, 4 }, //
					{ 4, 1, 5 }, //
					{ 3, 3, 7 }, //
					{ 6, 1, 15 }, //
					{ 6, 2, 17 }, //
					{ 6, 3, 21 }, //
					{ 6, 4, 29 }, //
					{ 6, 5, 45 }, //
					{ 7, 6, 77 }, //
					{ 7, 32, 141 } /* high */
			},
			// B14
			{   { 3, 0, -2 }, //
					{ 3, 0, -1 }, //
					{ 1, 0, 0 }, //
					{ 3, 0, 1 }, //
					{ 3, 0, 2 } //
			},
			// B15
			{   { 7, 4, -24 }, //
					{ 6, 2, -8 }, //
					{ 5, 1, -4 }, //
					{ 4, 0, -2 }, //
					{ 3, 0, -1 }, //
					{ 1, 0, 0 }, //
					{ 3, 0, 1 }, //
					{ 4, 0, 2 }, //
					{ 5, 1, 3 }, //
					{ 6, 2, 5 }, //
					{ 7, 4, 9 }, //
					{ 7, 32, Integer.MIN_VALUE, 999 }, /* low */
					{ 7, 32, 25 } /* high */
			} };

	private static HuffmanTable STANDARD_TABLES[] = new HuffmanTable[TABLES.length];

	public static HuffmanTable getTable(int number) {
		HuffmanTable table = STANDARD_TABLES[number - 1];
		if (table == null) {
			table = new StandardTable(TABLES[number - 1]);
			STANDARD_TABLES[number - 1] = table;
		}

		return table;
	}
}
