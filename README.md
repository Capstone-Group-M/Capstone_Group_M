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

## Features
- Obtain NOTAMs (Pending)
- Filter NOTAMs (Pending)
- Display NOTAMs (Pending)
  
---

## Technologies Used
- Language(s): Python, JavaScript
- Libraries / Frameworks: React
- Tools: Git Hub, Git, FAA NOTAMs API, FireBase


## Installation
Steps to set up and run the project locally.

```bash
git clone https://github.com/your-username/project-name.git
cd project-name
