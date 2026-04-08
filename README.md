# 💰 Crowdfunding Finance Management System

## 👥 Team Members

* **Mohammed Ehan Sheikh** (PES2UG23CS345)
* **Mohit Kumar** (PES2UG23CS350)
* **Mrigank Jain** (PES2UG23CS352)
* **Nalin Gabriel** (PES2UG23CS360)

---

## 📌 Project Description

The **Crowdfunding Finance Management System** is designed to organize and manage online fundraising activities in a structured and efficient manner.

The system includes key entities such as:

* Donor
* Fundraiser
* Administrator
* Transaction
* Visit
* Payroll

Donors can explore campaigns, visit fundraisers, and make contributions. Fundraisers represent campaigns with details like title, goal amount, deadline, and progress. Administrators manage the system and handle fund distribution through payroll.

The system ensures **transparency, proper tracking, and efficient management** of crowdfunding activities. 

---

## 🧱 MVC Architecture

* **Model** → Entities (`Donor`, `Fundraiser`, `Transaction`, etc.)
* **View** → Thymeleaf templates (UI)
* **Controller** → Handles user requests and flow

---

## 🧠 GRASP Principles Used

1. **Controller**
   Implemented using Spring Controllers to handle HTTP requests and delegate tasks.

2. **Information Expert**
   Business logic is handled in the Service layer, which has required data.

3. **Low Coupling**
   Layers (Controller → Service → Repository) are loosely coupled using dependency injection.

4. **High Cohesion**
   Each class has a single responsibility (Model, Service, Repository separation).

---

## 🗄️ Database Design

The system uses MySQL database with tables:

* Donor
* Fundraiser
* Transaction
* Administrator
* Visit
* Payroll

---

## ⚙️ Technologies Used

* Java (Spring Boot)
* Spring Data JPA
* MySQL
* Thymeleaf
* Maven

---

## 🚀 Features

* Create and manage fundraising campaigns
* Donate to campaigns
* Track transactions
* Admin management
* Payroll handling
* Visit tracking

---

## ⚙️ Setup Instructions

1. Clone the repository:

```bash
git clone https://github.com/mohit78999/crowdfunding-finance-system.git
```

2. Configure MySQL in `application.properties`

3. Run the project:

```bash
mvn spring-boot:run
```

---

## 🏁 Conclusion

This project provides a structured and transparent way to manage crowdfunding activities, ensuring efficient tracking of donations, campaigns, and fund distribution.
