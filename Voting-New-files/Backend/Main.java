package com.voting.connection;

import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        VoterDAO voterDAO = new VoterDAO();
        VoteDAO voteDAO = new VoteDAO();
        PartyDAO partyDAO = new PartyDAO();

        System.out.println("===== ONLINE VOTING SYSTEM =====");

        String again;

        do {
            processVoter(sc, voterDAO, voteDAO, partyDAO);

            System.out.print("\nNext voter? (y/n): ");
            again = sc.nextLine().trim();

        } while (again.equalsIgnoreCase("y"));

        voteDAO.showResults();
        System.out.println("\nThank you. Voting closed.");
        sc.close();
    }

    private static void processVoter(Scanner sc, VoterDAO voterDAO,
                                     VoteDAO voteDAO, PartyDAO partyDAO) {

        // 1. Voter ID
        System.out.print("\nEnter Voter ID (example SYK2121150): ");
        String voterId = sc.nextLine().trim().toUpperCase();

        if (!VoterDAO.isValidFormat(voterId)) {
            System.out.println("Invalid Voter ID. Use 3 capital letters + 7 digits.");
            return;
        }

        // 2. New voter -> register, existing voter -> login
        if (!voterDAO.exists(voterId)) {

            System.out.println("New voter. Please register.");

            System.out.print("Enter your name: ");
            String name = sc.nextLine().trim();

            System.out.print("Create a password: ");
            String newPassword = sc.nextLine().trim();

            if (name.isEmpty() || newPassword.isEmpty()) {
                System.out.println("Name and password cannot be empty.");
                return;
            }

            if (!voterDAO.register(voterId, name, newPassword)) {
                return;
            }

            System.out.println("Registered successfully.");

        } else {

            System.out.print("Enter Password: ");
            String password = sc.nextLine().trim();

            Voter voter = voterDAO.login(voterId, password);

            if (voter == null) {
                System.out.println("Wrong password.");
                return;
            }

            System.out.println("Welcome, " + voter.getVoterName());
        }

        // 3. Already voted?
        if (voteDAO.hasVoted(voterId)) {
            System.out.println("You are already voted. One voter can vote only one time.");
            return;
        }

        // 4. Show parties
        List<Party> parties = partyDAO.getAllParties();

        System.out.println("\nTamil Nadu Political Parties");
        for (Party p : parties) {
            System.out.println(p.getPartyId() + " - "
                    + p.getPartyName() + " (" + p.getPartyCode() + ")");
        }

        // 5. Vote
        System.out.print("\nEnter party number to vote: ");
        int partyId;

        try {
            partyId = Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid party number. Vote not saved.");
            return;
        }

        boolean validParty = false;
        for (Party p : parties) {
            if (p.getPartyId() == partyId) {
                validParty = true;
                break;
            }
        }

        if (!validParty) {
            System.out.println("Party number not in the list. Vote not saved.");
            return;
        }

        if (voteDAO.submitVote(voterId, partyId)) {
            System.out.println("Your vote has been recorded. Thank you!");
        } else {
            System.out.println("Vote could not be saved.");
        }
    }
}