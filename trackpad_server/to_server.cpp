//
//  to_server.cpp
//  
//
//  Created by Alden Perrine on 5/7/18.
//
//

#include <iostream>
#include <string>

#include <stdio.h>
#include <netinet/in.h>
#include <netdb.h>
#include <stdlib.h>
#include <errno.h>

int main(int argc, char** argv)
{
    if(argc != 2)
    {
        std::cerr << "Wrong arguments" << std::endl;
        exit(1);
    }
    
    int port = atoi(argv[1]);
    
    int err, serverfd;
    struct sockaddr_in address;
    struct hostent* server;
    if((serverfd = socket(AF_INET, SOCK_STREAM, 0)) < 0)
    {
        err = errno;
        const char* errstr = strerror(err);
        std::cerr << "Error creating socket ::" << std::string(errstr) << std::endl;
        exit(1);
    }
    if((server = gethostbyname("127.0.0.1")) == NULL)
    {
        err = h_errno;
        const char* errstr = hstrerror(err);
        fprintf(stderr, "Failed to find the server\r\n%s\r\n", errstr);
        exit(1);
    }
    bzero((char *) &address, sizeof(address));
    address.sin_family = AF_INET;
    bcopy((char *)server->h_addr,
          (char *)&address.sin_addr.s_addr,
          server->h_length);
    address.sin_port = htons((short) port);
    if (connect(serverfd,(struct sockaddr *) &address,sizeof(address)) < 0)
    {
        err = errno;
        const char* errstr = strerror(err);
        std::cerr << "Error connecting to server ::" << std::string(errstr) << std::endl;
        exit(1);
    }
    
    std::string line;
    while(line != "quit")
    {
      std::getline(std::cin, line);
      write(serverfd, line.c_str(), line.size());
      write(serverfd, "\n", 1);
    }
    
    close(serverfd);
    return 0;
}

