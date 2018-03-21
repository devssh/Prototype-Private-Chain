# Prototype blockchain on Java, to demonstrate signature verification and Proof of Work
The purpose is to verify signatures signed by authorities and mined using Proof of Work at difficulty 3.
We have 2 types of transactions:
1. Create token
2. Redeem token
The blocks are created every 5 seconds on average if there is a pending transaction.

## To run the demo on JDK8
```
./gradlew bootRun
```

and visit localhost:8080

## Workflow
1. Visit localhost:8080 to check this list of instructions and verify health of the server.
2. Visit localhost:8080/coupons-explorer to see the current state of the chain. The data related to this is stored on blocks.dat.
To reset the project to its initial state, remove everything except the genesis block(line1 of blocks.dat)
3. Visit localhost:8080/authorized to view the public keys of the authorities. Everyone in the blockchain can see the public keys of these trusted delegates.
4. Visit localhost:8080/create?sign=MFkwEw&message=MESSAGE_HERE&owner=OWNER_HERE&aadhar=AADHAR_HERE as shown in the image below.
This endpoint is a simulation provided to show how an authorized miner(in this case its the authority named "Dev") would sign transactions after verifying the integrity.
5. Visit the coupons page to verify that the transactions have been updated. We usually wait for the new transactions to be at least 'x' blocks deep based on the cost of mining a block. Thus if a block costs $1 to mine we would wait 100 transactions to verify that it has been added.(Now it costs at least $100 to modify.) We don't have to spend $1 to mine it, we can spend/create virutal tokens for that amount and distribute it free to users to try out the service or to incentivise miners to copy the blockchain and dedicate computing resources.
6. Verify the transaction in localhost:8080/verify-form with the information provided in the coupons page.(which uses a public algorithm so that anyone can verify the data without having to depend on this page. A helper has been added in the create page to display the data needed to fill this form. The signatures are available in the coupons page)
Verification costs nothing compared to creating blocks.


## Home screen
![](https://cdn.rawgit.com/devssh/Prototype-Private-Chain/392e33d9/Home%20screen.png)

## Initial chain
![](https://cdn.rawgit.com/devssh/Prototype-Private-Chain/392e33d9/Initial%20chain.png)

## Authority Dev processes transaction
![](https://cdn.rawgit.com/devssh/Prototype-Private-Chain/392e33d9/Request%20transaction.png)

## Updated state of chain
![](https://cdn.rawgit.com/devssh/Prototype-Private-Chain/392e33d9/Updated%20chain.png)

## Authorities public keys
![](https://cdn.rawgit.com/devssh/Prototype-Private-Chain/392e33d9/Authorities.png)

## Any user can verify the chain's signatures using this interface or using the public algorithm
![](https://cdn.rawgit.com/devssh/Prototype-Private-Chain/392e33d9/Verification.png)
