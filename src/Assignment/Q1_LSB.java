package Assignment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class Q1_LSB {
	static int secretMessageLength;

	public static void main(String[] args) {
		Scanner input = new Scanner(System.in);

		System.out.println("Choose an action: 1.Embed  2.Extract");
		int choice = input.nextInt();
		input.nextLine(); // Consume the newline character left from the previous input
		while (choice <= 2) {
			if (choice == 1) {

				String secretMessage = readSecretMessageFromFile(
						"C:\\Users\\User\\Desktop\\testing\\secret_message.txt");

				File coverImageFile = new File("C:\\Users\\User\\Desktop\\testing\\p1.jpg");

				int messageLength = secretMessage.length();
				secretMessageLength = messageLength;
				try {
					BufferedImage coverImage = ImageIO.read(coverImageFile);
					if (messageLength * 8 > coverImage.getWidth() * coverImage.getHeight()) {
						System.out.println("Message is too large for the image.");
						System.exit(2);
					}

					// Calculate the chunk size for message splitting
					int chunkSize = getChunkSize(messageLength);
					ArrayList<String> messageChunks = new ArrayList<>();

					// Split the message into chunks
					while (secretMessage.length() > chunkSize) {
						String chunk = secretMessage.substring(0, chunkSize);
						messageChunks.add(chunk);
						secretMessage = secretMessage.substring(chunkSize);
					}
					messageChunks.add(secretMessage);

					BufferedImage stegoImage = embedMessage(coverImage, messageChunks);
					ImageIO.write(stegoImage, "PNG", new File("C:\\Users\\User\\Desktop\\testing\\p2.png"));

					System.out.println("Embedding successful.");
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else if (choice == 2) {
				File stegoImageFile = new File("C:\\Users\\User\\Desktop\\testing\\p2.png");
				try {
					BufferedImage stegoImage = ImageIO.read(stegoImageFile);
					String extractedMessage = extractMessage(stegoImage, secretMessageLength);
					System.out.println("Extracted Message: \n" + extractedMessage);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			System.out.println("Choose an action: 1. Embed 2. Extract");
			choice = input.nextInt();
			input.nextLine(); // Consume the newline character left from the previous input
		}
	}

	// Read the secret message from a text file
	public static String readSecretMessageFromFile(String filePath) {
		try {
			// Create a FileReader to read the text file
			FileReader fileReader = new FileReader(filePath);

			// Create a BufferedReader for efficient reading
			BufferedReader bufferedReader = new BufferedReader(fileReader);

			// Read the contents of the file line by line
			StringBuilder secretMessage = new StringBuilder();
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				secretMessage.append(line);
				secretMessage.append(System.lineSeparator()); // Add line separator if needed
			}

			// Close the resources
			bufferedReader.close();
			fileReader.close();

			// Return the secret message as a string
			return secretMessage.toString();
		} catch (IOException e) {
			e.printStackTrace();
			return ""; // Return an empty string in case of an error
		}

	}

	// Calculate the chunk size for message splitting
	public static int getChunkSize(int messageLength) {
		return messageLength / 5;
	}

	public static BufferedImage embedMessage(BufferedImage image, ArrayList<String> array) {
		// Create a new stego image with the same dimensions as the cover image
		BufferedImage StegoImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);

		int x = 0;
		int y = 0;
		int chunkIndex = 0;
		while (chunkIndex < array.size()) {
			int charIndex = 0;
			while (charIndex < array.get(chunkIndex).length()) {
				int character = array.get(chunkIndex).charAt(charIndex);
				String binaryData = Integer.toBinaryString(character);
				int length = 8 - binaryData.length();
				// to do all characters 8 bit by adding 0
				for (int b = 0; b < length; b++) {
					binaryData = "0" + binaryData;
				}
				for (int h = 0; h < binaryData.length(); h++) {
					if (y < image.getHeight()) {
						int originalRGB = image.getRGB(x, y);
						int bit = Integer.parseInt(binaryData.charAt(h) + "");
						originalRGB = (originalRGB & 0xFFFFFFFE) | (bit & 1);
						StegoImage.setRGB(x, y, originalRGB);
						y++;
					} else {
						x++;
						y = 0;
					}
				}

				charIndex++;

			}

			chunkIndex++;

		}

		// Check if the new image is not incomplete
		while (x < image.getWidth()) {
			while (y < image.getHeight()) {
				int originalRGB = image.getRGB(x, y);
				StegoImage.setRGB(x, y, originalRGB);
				y++;
			}
			if (y == image.getHeight()) {
				y = 0;
			}

			x++;
		}

		return StegoImage;
	}

	public static String extractMessage(BufferedImage image, int length) {
		String binaryMessage = "";
		String extractedMessage = "";
		int bitCounter = 0;
		int chunkCounter = 0;
		int i = 0;
		while (i < image.getWidth()) {
			int j = 0;
			while (j < image.getHeight()) {
				int pixel = image.getRGB(i, j);
				int lsb = pixel & 0x01;// this line to get the last seg
				binaryMessage = binaryMessage + lsb;// to save 7 bit

				bitCounter++;
				if (bitCounter == 8) {
					bitCounter = 0;
					int decimalNum = Integer.parseInt(binaryMessage, 2);
					char character = (char) decimalNum;
					extractedMessage = extractedMessage + character;// to save character
					binaryMessage = "";
					chunkCounter++;
				}

				if (chunkCounter == length) {
					break;
				}
				j++;
			}

			i++;
			break;
		}

		return extractedMessage;

	}
}
