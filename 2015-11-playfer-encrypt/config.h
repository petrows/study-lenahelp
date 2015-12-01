 
#define MATRIX_X 5
#define MATRIX_Y 5

#include <stdio.h>
#include <string.h>
#include <iostream>
#include <vector>

using namespace std;

char matrix[MATRIX_Y][MATRIX_X];
const char * alphabet = "ABCDEFGHIJKLMNOPRSTUVWXYZ";

bool alphabetContains(char c)
{
	for (int x=0; x<strlen(alphabet); x++)
	{
		if (alphabet[x] == c) return true;
	}
	return false;
}

void matrixInit()
{
	for (int y=0; y<MATRIX_Y; y++)
	{
		for (int x=0; x<MATRIX_X; x++)
		{
			matrix[y][x] = 0x00;
		}
	}
}

// Print encrypted pairs text top, bigramm
struct pairTopItem {
	char a;
	char b;
	int count;
	
	pairTopItem() { count = 0; }
	pairTopItem(char inA, char inB) { a = inA; b = inB; count = 1; }
	pairTopItem(std::string s) { a = s[0]; b = s[1]; count = 1; }
	bool operator ==(const pairTopItem &other) { return a == other.a && b == other.b; }
};

void printPairsTop(string text)
{
	vector<pairTopItem> pairsTop;
	for (int x=1; x<text.size(); x+=2)
	{
		// Check - this pair exists in top?
		bool is_exists = false;
		for (int z=0; z<pairsTop.size(); z++)
		{
			if (pairsTop[z].a == text[x-1] && pairsTop[z].b == text[x])
			{
				// Exists!!
				is_exists = true;
				// Add +1 to pair counter
				pairsTop[z].count = pairsTop[z].count + 1;
				break;
			}
		}
		if (!is_exists)
		{
			// Pair not in top - add it 
			pairTopItem item;
			item.a = text[x-1];
			item.b = text[x];
			item.count = 1;
			pairsTop.push_back(item);
		}
	}
	
	// Now - sort The Top
	// Сортирвока - "пузырьком"
	bool need_sort = false;
	do {
		need_sort = false;
		for (int z=1; z<pairsTop.size(); z++)
		{
			// Неверный порядок?
			if (pairsTop[z-1].count < pairsTop[z].count)
			{
				// Меняем местами эелементы массива
				pairTopItem item;
				item = pairsTop[z-1];
				pairsTop[z-1] = pairsTop[z];
				pairsTop[z] = item;
				need_sort = true;
			}
		}
	} while (need_sort);
	
	// Now print The Top
	cout << "Word pairs count top:" << endl;
	for (int z=0; z<pairsTop.size(); z++)
	{
		cout << "#" << (z+1) << " - " << pairsTop[z].a << pairsTop[z].b << " - count " << pairsTop[z].count << endl;
	}
}

vector<pairTopItem> makeTextBigrams(vector<pairTopItem> inBigrams)
{
	vector<pairTopItem> out;
	
	bool is_exists = false;
	for (int x=0; x<inBigrams.size(); x++)
	{
		// Check - this pair exists in top?
		bool is_exists = false;
		for (int z=0; z<out.size(); z++)
		{
			if (out[z].a == inBigrams[x].a && out[z].b == inBigrams[x].b)
			{
				// Exists!!
				is_exists = true;
				// Add +1 to pair counter
				out[z].count = out[z].count + 1;
				break;
			}
		}
		if (!is_exists)
		{
			// Pair not in top - add it 
			pairTopItem item;
			item.a = inBigrams[x].a;
			item.b = inBigrams[x].b;
			item.count = 1;
			out.push_back(item);
		}
	}
	
	// Сортирвока - "пузырьком"
	bool need_sort = false;
	do {
		need_sort = false;
		for (int z=1; z<out.size(); z++)
		{
			// Неверный порядок?
			if (out[z-1].count < out[z].count)
			{
				// Меняем местами эелементы массива
				pairTopItem item;
				item = out[z-1];
				out[z-1] = out[z];
				out[z] = item;
				need_sort = true;
			}
		}
	} while (need_sort);
	
	return out;
}

// Test - char exists in matrix?
bool matrixContains(char c)
{
	// Test char - A-Z?
	if (!alphabetContains(c)) return true;

	for (int y=0; y<MATRIX_Y; y++)
	{
		for (int x=0; x<MATRIX_X; x++)
		{
			if (matrix[y][x] == c) return true;
		}
	}
	return false;
}

const char * encryptPair(char a, char b)
{
	char aOut = a;
	char bOut = b;
	static char bufIn[3];
	static char bufOut[3];
	sprintf(bufIn, "%c%c", a, b);

	// Calc letters pos
	int posAx = 0;
	int posAy = 0;
	int posBx = 0;
	int posBy = 0;
	// Find the letters a & b in matrix and save pos (X,Y) in vars
	for (int y=0; y<MATRIX_Y; y++)
	{
		for (int x=0; x<MATRIX_X; x++)
		{
			if (matrix[y][x] == a)
			{
				posAx = x;
				posAy = y;
			}
			if (matrix[y][x] == b)
			{
				posBx = x;
				posBy = y;
			}
		}
	}


	// 1. detect - one line?
	if (posAy == posBy) {
		//cout << "line equal " << a << "," << b << endl;
		aOut = matrix[posAy][ (posAx == MATRIX_X-1) ? 0 : posAx+1 ];
		bOut = matrix[posAy][ (posBx == MATRIX_X-1) ? 0 : posBx+1 ];

	// 2. detect - one row?
	} else if (posAx == posBx) {
		// cout << "row equal " << a << "," << b << endl;
		aOut = matrix[ (posAy == MATRIX_Y-1) ? 0 : posAy+1 ][posAx];
		bOut = matrix[ (posBy == MATRIX_Y-1) ? 0 : posBy+1 ][posAx];

	// 3. Swap X and Y pos (other corners)
	} else {
		aOut = matrix[ posBy ][ posAx ];
		bOut = matrix[ posAy ][ posBx ];
	}

	sprintf(bufOut, "%c%c", aOut, bOut);
	// cout << bufIn << " => " << bufOut << endl;

	return bufOut;
}

const char * decryptPair(char a, char b)
{
	char aOut = a;
	char bOut = b;
	static char bufIn[3];
	static char bufOut[3];
	sprintf(bufIn, "%c%c", a, b);

	// Calc letters pos
	int posAx = 0;
	int posAy = 0;
	int posBx = 0;
	int posBy = 0;
	// Find the letters a & b in matrix and save pos (X,Y) in vars
	for (int y=0; y<MATRIX_Y; y++)
	{
		for (int x=0; x<MATRIX_X; x++)
		{
			if (matrix[y][x] == a)
			{
				posAx = x;
				posAy = y;
			}
			if (matrix[y][x] == b)
			{
				posBx = x;
				posBy = y;
			}
		}
	}


	// 1. detect - one line?
	if (posAy == posBy) {
		//cout << "line equal " << a << "," << b << endl;
		aOut = matrix[posAy][ (posAx == 0) ? MATRIX_X-1 : posAx-1 ];
		bOut = matrix[posAy][ (posBx == 0) ? MATRIX_X-1 : posBx-1 ];

		// 2. detect - one row?
	} else if (posAx == posBx) {
		// cout << "row equal " << a << "," << b << endl;
		aOut = matrix[ (posAy == 0) ? MATRIX_Y-1 : posAy-1 ][posAx];
		bOut = matrix[ (posBy == 0) ? MATRIX_Y-1 : posBy-1 ][posAx];

		// 3. Swap X and Y pos (other corners)
	} else {
		aOut = matrix[ posBy ][ posAx ];
		bOut = matrix[ posAy ][ posBx ];
	}

	sprintf(bufOut, "%c%c", aOut, bOut);
	cout << bufIn << " => " << bufOut << endl;

	return bufOut;
}
