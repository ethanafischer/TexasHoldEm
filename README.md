# Texas Hold'em Poker Program

## Introduction

This program is a Texas Hold'em poker simulator. It can simulate games with 2 to 22 players in a single hand. The program calculates the strongest hand for each player and determines the winner of the hand.

## How to Run the Program

To run the program, simply execute the main method of the Poker class. You will be prompted to enter the number of players and the number of hands you want to simulate. 

## How the Program Works

The program uses a standard 52-card deck to simulate games of Texas Hold'em. Each player is dealt two cards, known as "hole" cards, and then five community cards are dealt in the center of the table. Players can use any combination of their hole cards and the community cards to make the best possible five-card hand. 

The program calculates the best possible hand for each player using a combination of brute-force and heuristic algorithms. It then compares each player's hand to determine the winner. After the simulation is complete, the program will output the frequency of the different hands winning, as well as the runtime of the program.

## Performance

The runtime for simulating 100,000 hands on a table of 9 players averages around 30 seconds. Thus, the average computational runtime for the program is 300 microseconds per hand. The actual runtime may vary depending on the hardware specifications of your computer.

## Limitations

This program has a maximum limit of 22 players per hand. This is because a standard 52-card deck cannot accommodate more than 22 players. However, it's important to note that most Hold'em games typically only have between 6-9 players at a table. 
Additionally, the program is limited to simulating games of Texas Hold'em and does not support other variations of poker.
Note that the program is not intended to be a fully-featured poker playing application, and as such, it does not currently support betting with chips.

## Credits

Ethan Fischer, 2023
