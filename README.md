# Texas Hold'em Poker Program

## Introduction

This program is a Texas Hold'em poker simulator. It can simulate games with 1 to 22 players in a single hand. The program calculates the strongest hand for each player and determines the winner of the hand.

The program uses a standard 52-card deck to simulate games of Texas Hold'em. Each player is dealt two cards, known as "hole" cards, and then five community cards are dealt in the center of the table. Players can use any combination of their hole cards and the community cards to make the best possible five-card hand. 

The program calculates the best possible hand for each player using a combination of brute-force and heuristic algorithms. It then compares each player's hand to determine the winner. After the simulation is complete, the program will output the frequency of the different hands, as well as the runtime of the program.

Frequency of hand rankings (9 players, 1000000 hands):
* Royal Flush    : 0.00% (282/9000000)
* Straight Flush : 0.03% (2518/9000000)
* Four of a Kind : 0.17% (15479/9000000)
* Full House     : 2.60% (233878/9000000)
* Flush          : 3.03% (272706/9000000)
* Straight       : 4.62% (415604/9000000)
* Three of a Kind: 4.83% (434783/9000000)
* Two Pair       : 23.48% (2113483/9000000)
* One Pair       : 43.85% (3946063/9000000)
* High Card      : 17.39% (1565204/9000000)

## Performance

The runtime for simulating 100,000 hands on a table of 9 players averages around 30 seconds. Thus, the average computational runtime for the program is 300 microseconds per hand. The actual runtime may vary depending on the hardware specifications of your computer.

## Limitations

This program has a maximum limit of 22 players per hand. This is because a standard 52-card deck cannot accommodate more than 22 players. However, it's important to note that most Hold'em games typically only have between 6-9 players at a table. 
Additionally, the program is limited to simulating games of Texas Hold'em and does not support other variations of poker.
Note that the program is not intended to be a fully-featured poker playing application, and as such, it does not currently support betting with chips.

## Credits

Ethan Fischer, 2023