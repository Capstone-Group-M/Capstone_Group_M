# # Flight Message System

## Description
This project sets out to solve some of the common issues that pilots deal with when it comes to NOTAMs (Notice to Air Missions). NOTAMs provide
important information about flights like runway closures or airspace restrictions or malfunctioning equipment. However, it also includes some 
not-so-important information—notices like a construction crane twenty miles away from the nearest airport. Currenttly NOTAMs make difficult to 
get a good understanding of the hazards pilots are likely to face. The system will fetch all NOTAMs that pertain to a given flight and match all 
NOTAMs returned from a regular flight briefing. The system will obtain NOTAM data fetched via the FAA’s API Portal. It will focus 
on flight in the United States and exclude Hawaii and Alaska. 

Example:
A pilot recieves over a hundred NOTAMs for his flight, our system will filter out the most important NOTAMs and display them first. 

---
## Repo Structure

```
.
├── README.md
├── Ticket_2_Unit_Test_Examples
│   ├── NotamUtils.cpp
│   ├── NotamUtils.java
│   ├── NotamUtils.py
│   ├── NotamUtilsTest.cpp
│   ├── NotamUtilsTest.java
│   └── NotamUtilsTest.py
├── backend
│   ├── Instructions.txt
│   ├── pom.xml
│   ├── src
│   │   ├── main
│   │   │   ├── java
│   │   │   │   └── com
│   │   │   │       └── notam
│   │   │   │           ├── controller
│   │   │   │           │   └── Main.java
│   │   │   │           └── model
│   │   │   │               └── NOTAM.java
│   │   │   └── resources
│   │   └── test
│   │       ├── java
│   │       └── resources
│   │           └── mock_notams.json
│   └── target
│       ├── classes
│       │   └── com
│       │       └── notam
│       │           ├── controller
│       │           │   └── Main.class
│       │           └── model
│       │               ├── NOTAM$NotamTranslation.class
│       │               └── NOTAM.class
│       └── test-classes
│           └── mock_notams.json
├── cli
│   └── NotamCLI.java
├── config
├── docs
│   ├── CapstoneArchitecture.drawio
│   ├── CapstoneArchitecture.drawio.html
│   └── notam_characteristics.md
├── frontend
│   ├── README.md
│   ├── eslint.config.js
│   ├── index.html
│   ├── package-lock.json
│   ├── package.json
│   ├── public
│   │   └── vite.svg
│   ├── src
│   │   ├── App.css
│   │   ├── App.jsx
│   │   ├── assets
│   │   │   └── react.svg
│   │   ├── index.css
│   │   └── main.jsx
│   └── vite.config.js
├── proto
└── scripts
```

## Features
- Obtain NOTAMs (Pending)
- Filter NOTAMs (Pending)
- Display NOTAMs (Pending)
  
---

## Team Contributions & Responsibilities
### Product Owner
- Communicates with the mentor and other stakeholders
- Creates and prioritizes tickets in the product backlog
- Assigns tickets to appropriate sprints based on project goals
- Incorporates feedback from sprints into future sprint planning

### Scrum Master
- Facilitates the Scrum process and ensures adherence to Scrum practices
- Assigns sprint tickets to team members
- Leads sprint planning, daily stand-ups, and retrospectives
- Tracks sprint progress
  
### Shared Team Responsibilities
- Researching the NOTAM system and FAA API
- Implementing NOTAM data retrieval, parsing, and filtering logic
- Developing and executing unit tests
- Designing and improving the user interface
- Writing and maintaining project documentation
- Participating in sprint demos, reviews, and retrospectives
- Iterating on the system based on mentor and SME feedback


## Technologies Used
- Language(s): Java, JavaScript
- Libraries / Frameworks: React
- Tools: Git Hub, Git, FAA NOTAMs API, FireBase, Render


## Installation
Steps to set up and run the project locally.

```bash
git clone https://github.com/your-username/project-name.git
cd project-name
```


## Progress Plan
**All dates/goals are subject to change based on feedback and development progress*

### Sprint 1
End date: February 21

- Establish clear understanding of NOTAM
- Research NOTAM structure and FAA API
- Define core project features
- Define NOTAM “usefulness” criteria
- Set up GitHub repository
- Implement API connection and test data retrieval

### Sprint 2
End date: March 13

- Implement NOTAM parsing to separate raw text into structured fields
- Develop unit tests and test parsing functionality
- Implement a basic way to display parsed NOTAM information
- Implement NOTAM usefulness classification and prioritization
- End Goal: Have a Minimum-Viable-Product (MVP)

### Sprint 3
End date: April 18

- Improve filtering
  - Contingent on feedback 
- Improve UI

### Sprint 4
End date: May 9

- Improve performance and code quality
- Prepare final demo


