
# GitHub Repository Lister API

A simple REST API that fetches a list of a specified user's public, non-forked GitHub repositories, along with branch information for each.

## Table of Contents
1.  [Project Overview](#project-overview)
2.  [Getting Started](#getting-started)
    *   [Prerequisites](#prerequisites)
    *   [Configuration](#configuration)
    *   [Installation & Running](#installation--running)
3.  [API Documentation](#api-documentation)
    *   [Get User Repositories](#get-user-repositories)
4.  [Technologies Used](#technologies-used)

## Project Overview
This application provides a single REST endpoint that interacts with the public GitHub API. When given a GitHub username, the service retrieves a list of their repositories that are not forks. For each repository, the API also appends a list of its branches, including the branch name and the SHA of its latest commit.


## Getting Started

### Prerequisites
*   **Java 21** or newer

### Additional
*   **GitHub Personal Access Token for more requestes**
### Configuration
To avoid hitting the GitHub API's strict rate limits for unauthenticated requests, the application accepts a Personal Access Token.

You can configure the token in one of the following ways:

1.  **Environment Variable (Recommended)**:
    Set an environment variable named `GIT_API_KEY` with your token value.
    ```bash
    export GIT_API_KEY="ghp_YourPersonalAccessToken"
    ```

2.  **Properties File**:
    Add the following line to the `src/main/resources/application.properties` and `test/main/resources/application-test.properties`:
    ```properties
    passwd.gittoken=ghp_YourPersonalAccessToken
    ```
3. **Additional step for caching**
   If you want version which contains caching with Postgresql,
   download Postgresql switch to branch **feature/caching** and enter credentials in properties both src and test.
    ```properties
    
    spring.datasource.url = ${POSTGRES_URL}
    spring.datasource.username = ${USER}
    spring.datasource.password = ${PASSWD}
    
    ```

### Installation & Running

1.  **Clone the repository**
    ```bash
    git clone https://github.com/PsaiduPL/Atiperia_task.git
    cd Atiperia_task
    ```
Main Version is on branch main.

2.  **Run the application using the Maven wrapper**
    ```bash
    ./mvnw spring-boot:run
    ```
    The application will start on the default port `8080`.

## API Documentation

### Get User Repositories
Retrieves a list of non-forked repositories and their branches for a given GitHub user.

*   **URL:** `/api/{nickname}`
*   **Method:** `GET`
*   **URL Params:**

| Parameter  | Type     | Description                            |
| :--------- | :------- | :------------------------------------- |
| `nickname` | `String` | **Required.** The GitHub username. |

---

#### Sample Request (cURL)
```bash
curl http://localhost:8080/api/Exampl
```
---

#### Success Response (`200 OK`)

Returns a JSON array of repository objects.

```json
[
  {
    "login": "Exampl",
    "repositoryName": "ExamCI",
    "branches": [
      {
        "name": "master",
        "sha": "3891d715ad0f4903d1eab5877e05a721fe2553a7"
      }
    ]
  }
]
```
---

#### Error Responses

*   **Code:** `404 Not Found` <br/>
    **Reason:** The user with the provided `nickname` does not exist on GitHub.

    ```json
    {
    "status": 404,
    "message": "User doesn't exists"
    }
    ```

*   **Code:** `500 Internal Server Error` <br/>
    **Reason:** An unexpected server-side error occurred while processing the request.

*   **Code:** `503 Service Unavailable` <br/>
    **Reason:** The service is temporarily unavailable, likely due to issues communicating with the GitHub API.

## Technologies Used
*   **Spring Boot ** - Core framework
*   **Java 21** - Programming language
*   **Maven** - Dependency management and build tool
*   **Spring Web (RestTemplate)** - For communicating with the external REST API
