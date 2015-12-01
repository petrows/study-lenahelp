#include <iostream>
#include <stdio.h>

#include "config.h"

using namespace std;

void usage()
{
	cout << "Use program <file.txt>" << endl;
}

int main (int argc, char * argv[])
{
	matrixInit();

	const char * password = "playfair example";

	if (argc < 2)
	{
		usage();
		return 1;
	}

	const char * fileName = argv[1];

	FILE * f = fopen(fileName, "r");
	if (!f)
	{
		cout << "Cant open file " << fileName << endl;
		return 1;
	}

	fseek(f, 0, SEEK_END);
	int fileSize = ftell(f);

	char * fileData = new char[fileSize+1];
	fseek(f, 0, SEEK_SET);
	fread(fileData, fileSize, 1, f);
	fclose(f);

	cout << "Read file '" << fileName << "', size " << fileSize << " bytes" << endl;

	// Generate matrix

	// 1. Fill with password (not repeating)
	const char * passwordPos = password; // Pointer to password string
	const char * alphabetPos = alphabet; // Pointer to alphabet string
	for (int y=0; y<MATRIX_Y; y++)
	{
		for (int x=0; x<MATRIX_X; x++)
		{
			while (true)
			{
				// Cycle for get char and test it for repeated
				char passChar = toupper(*passwordPos);
				if (0x00 == passChar)
				{
					// End of string
					// Fill with the alphabet
					passChar = toupper(*alphabetPos);
					alphabetPos++;
				} else {
					passwordPos++; // Go to the next char
				}

				if (matrixContains(passChar))
				{
					// This letter already was in matrix, skip now
					continue;
				} else {
					// cout << "Add " << passChar << endl;
					matrix[y][x] = passChar; // Add to the matrix
					break;
				}
			}
		}
	}

	// Print matrix
	cout << " = Matrix = " << endl;

	for (int y=0; y<MATRIX_Y; y++)
	{
		cout << " ";
		for (int x=0; x<MATRIX_X; x++)
		{
			cout << matrix[y][x] << " ";
		}
		cout << endl;
	}
	cout << " ========== " << endl;

	// 2. Fix up The Message - encrypt only chars from alphabet, fill up to 2 symbols
	char * messageText = new char[fileSize + 2];
	bzero(messageText, fileSize+2);
	for (int x=0; x<fileSize; x++)
	{
		char c = toupper(fileData[x]);
		if (!alphabetContains(c)) continue; // Skip all chars outsize alpabet
		messageText[strlen(messageText)] = c;
	}
	
	// messageText - text prepared for playfair, analyze it
	vector<pairTopItem> bigramsIn;
	vector<pairTopItem> bigramsOut;

	std::string out;

	// 2. Encrypt The Message!
	for (int x=0; x<strlen(messageText); x++)
	{
		if (strlen(messageText) == x+1)
		{
			// Last symbol
			// Add 'X'
			continue;
		}

		if (messageText[x] == messageText[x+1])
		{
			// Equal symbols
			bigramsIn.push_back(pairTopItem(messageText[x], 'X'));
			bigramsOut.push_back(pairTopItem(encryptPair(messageText[x], 'X')));
			continue;
		}

		bigramsIn.push_back(pairTopItem(messageText[x], messageText[x+1]));
		bigramsOut.push_back(pairTopItem(encryptPair(messageText[x], messageText[x+1])));
		x+=1;
	}

	// 3. Make analyzis
	bigramsIn = makeTextBigrams(bigramsIn); // remove duplicates and sort
	vector<pairTopItem> bigramsEncrypted = makeTextBigrams(bigramsOut);
	// 4. try to decrypt
	for (int x=0; x<bigramsOut.size(); x++)
	{
		cout << "Processing " << x << " : " << bigramsOut[x].count << endl;
		// try to find in bigrams vocabulary
		int bigrammFreq = 0;
		for (int z=0; z<bigramsEncrypted.size(); z++)
		{
			if (bigramsOut[x] == bigramsEncrypted[z])
			{
				// found! save freq
				bigrammFreq = bigramsEncrypted[z].count;
				break;
			}
		}

		if (0 == bigrammFreq) continue; // not found

		// now search decrypted text with this frequency
		// if there > 0 found, skip this bigramm
		int foundBigrammOpenCount = 0;
		pairTopItem foundBigrammOpen;
		for (int z=0; z<bigramsIn.size(); z++)
		{
			if (bigramsIn[z].count == bigrammFreq)
			{
				// found! save freq
				foundBigrammOpenCount++;
				foundBigrammOpen = bigramsIn[z];
			}
		}

		if (1 == foundBigrammOpenCount)
		{
			// found it!
			out.push_back(foundBigrammOpen.a);
			out.push_back(foundBigrammOpen.b);
		} else {
			// not found or multiply variants...
			out.append("..");
		}
	}

	FILE * fOut = fopen("encrypted.txt", "w");
	if (!fOut)
	{
		cout << "Cant open out file!" << endl;
		return 1;
	}

	fwrite(out.c_str(), out.size(), 1, fOut);
	fclose(fOut);

	cout << "Encrypted message size " << out.size() << " bytes, saved to 'encrypted.txt'" << endl;

	return 0;
}
