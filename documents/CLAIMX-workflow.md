### WORKFLOWS

---

## LOGIN
```mermaid
flowchart TD

A[LOGIN]

B[EMPLOYEE ENTERS EMAIL AND PASSWORD] --> A

A --> C{successful}

C -- YES --> D[jwt token is created]

C -- NO --> E[403]

```

---

## EMPLOYEE WORKFLOW
```mermaid

flowchart TD

A[CREATE CLAIM] --> B[ADD EXPENSE ITEM WITH CLAIM ID]

     A -.-> T[enter the TITLE of the claim]

B -.-> J["{
category: TRAVEL
description: flight to NY
amount: 55000
expenseDate: 2025-10-01
}"]

B --> C[SUBMIT CLAIM]

B --> D[GET CLAIM BY ID]

D --> E[GET ALL MY CLAIMS]

E --> F[GET CLAIMS BY STATUS]

B --> G[GET ITEMS BY CLAIM ID]

C -.-> S[submit the claim by claim id]

```
---

## MANAGER WORKFLOW

```mermaid

flowchart TD

A[LOGIN]

A -.-> L[login using email and password]

A --> B[GET ALL THE PENDING CLAIMS]

B --> C{ACCEPTED / REJECTED}

C --> D[ACCEPTED with comment]

C --> E[REJECTED with comment]

D --> F[
status: ACCEPTED
reviewDateUpdated
reviewCommentUpdated
]

E --> G[
status: REJECTED
reviewDateUpdated
reviewCommentUpdated
]

```

---
## FINANCE WORKFLOW

```mermaid
flowchart TD

A[LOGIN]

A -.-> L[email, password]

A --> B[GET ALL PENDING CLAIMS]

A --> C[VIEW PAID CLAIMS]

A --> D[PAY CLAIMS]

D -.-> I[claimId]

D --> E[Status: PAID]

```

---

### logins sample

```json
employee

 {
   "email": "prasid.employee@claimx.com",
   "password": "prasid@123"
 }

 {
  "email": "prajwal.employee@claimx.com",
  "password": "prajwal@123"
}

---

manager

---

{
  "email": "venkat.manager@claimx.com",
  "password": "venkat@123"
}


{
  "email": "mohan.manager@claimx.com",
  "password": "mohan@123"
}

---

finance

---


{
  "email": "akash.finance@claimx.com",
  "password": "akash@123"
}

---

admin

---
{
  "email": "admin@claimx.com",
  "password": "admin@123"
}

```

