//DNS Query Program
//Dated : 26/2/2007
// #include <netdb.h>
//Header Files
#define WIN32_MEAN_AND_LEAN
#include <iostream>
#include <winsock2.h>
#include <windows.h>
#include <ws2tcpip.h>
#include <process.h>
#include <stdio.h>
#include <stdlib.h>
#include <cstdlib>
#include <sstream>
#include <fstream> 
#include <cstring>
#include <string>
#include <cmath>
#include <algorithm>
#include "conio.h" 
#include <regex>

// Need to link with Ws2_32.lib, Mswsock.lib, and Advapi32.lib
#pragma comment (lib, "Ws2_32.lib")
#pragma comment (lib, "Mswsock.lib")
#pragma comment (lib, "AdvApi32.lib")

const int  REQ_WINSOCK_VER   = 2;	// Minimum winsock version required
const int  TEMP_BUFFER_SIZE  = 1500;
 
//List of DNS Servers registered on the system
char dns_server[100];

//Type field of Query and Answer
#define T_A 1 /* host address */
#define T_NS 2 /* authoritative server */
#define T_CNAME 5 /* canonical name */
#define T_SOA 6 /* start of authority zone */
#define T_PTR 12 /* domain name pointer */
#define T_MX 15 /* mail routing information */
 
//Function Declarations
struct hostent* dnsQuery (unsigned char*);
void ChangetoDnsNameFormat (unsigned char*,unsigned char*);
unsigned char* ReadName (unsigned char*,unsigned char*,int*);
void PrintDnsResponse(struct hostent*);
//DNS header structure
struct DNS_HEADER
{
    unsigned short id; // identification number
 
    unsigned char rd :1; // recursion desired
    unsigned char tc :1; // truncated message
    unsigned char aa :1; // authoritive answer
    unsigned char opcode :4; // purpose of message
    unsigned char qr :1; // query/response flag
 
    unsigned char rcode :4; // response code
    unsigned char cd :1; // checking disabled
    unsigned char ad :1; // authenticated data
    unsigned char z :1; // its z! reserved
    unsigned char ra :1; // recursion available
 
    unsigned short q_count; // number of question entries
    unsigned short ans_count; // number of answer entries
    unsigned short auth_count; // number of authority entries
    unsigned short add_count; // number of resource entries
};
 
//Constant sized fields of query structure
struct QUESTION
{
    unsigned short qtype;
    unsigned short qclass;
};
 
//Constant sized fields of the resource record structure
#pragma pack(push, 1)
struct R_DATA
{
    unsigned short type;
    unsigned short _class;
    unsigned int ttl;
    unsigned short data_len;
};
#pragma pack(pop)
 
//Pointers to resource record contents
struct RES_RECORD
{
    unsigned char *name;
    struct R_DATA *resource;
    unsigned char *rdata;
};
 
//Structure of a Query
typedef struct
{
    unsigned char *name;
    struct QUESTION *ques;
} QUERY;

using namespace std;

class DNS_Validator
{
    public:
        // Constructors
        DNS_Validator();
        // Methods
        bool isValid(string);

    private:
        // Methods
        bool checkTotalLength(string); // >=  253
        bool checkLabels(string);
        bool checkAlphaNumericDotsOrHyphens(string);
        bool checkNotOnlyDigits(string);
        bool checkNotEmpty(string);
        bool checkNotConsequtiveDots(string);
        bool checkLastCharIsNotSot(string);
        void printError(string);

};

int main(int argc, char *argv[]) 
{
	bool    pressButtonToExit = false;
    bool    quitPressed       = false;
	string  url; 
	string dnsIpAddress;
	DNS_Validator validator;
	if (argc !=2)  // Test for correct number of arguments
    {    
        cerr << "Usage: " << argv[0] 
             << " <DNS Server IP Address>" << endl;
        cout << "Press Enter to quit" << endl;
        cin.get();
        exit(1);
    }
    dnsIpAddress = argv[1];
    cout << "\nWelcome to DNS Parser\n========================"<<endl;
    // Start winsock
	WSADATA firstsock;
    printf("\nInitialising Winsock...");
    if (WSAStartup(MAKEWORD(2,2),&firstsock) != 0)
    {
        printf("Failed. Error Code : %d",WSAGetLastError());
        return 1;
    }
    printf("Initialised\n\n");
	dnsIpAddress.copy(dns_server,dnsIpAddress.length(),0);
	dns_server[dnsIpAddress.length()+1] = '\0';
	while(!pressButtonToExit)
    {
        cout <<"\nPlease Enter a URL to find its IP address or quit to Exit\n\n"<<endl;
        //cin >> url;
		char urlTemp[257];
		cin.getline (urlTemp , 257 );
		string url(urlTemp);
        transform(url.begin(), url.end(), url.begin(), ::tolower);
        if(url == "quit")
        {
            cout << "\nThank you for using DNS Parser.\n\nExiting....." <<endl;
			// close winsock
			cout << "Closing Winsock ... " ;	
			WSACleanup(); //Clean up Winsock
			cout << "Closed" << endl;	
            exit(0);
        }
        else
        {
			if(validator.isValid(url))
			{
				struct hostent* hostEntity = NULL;
				hostEntity = dnsQuery((unsigned char *)url.c_str());
				PrintDnsResponse(hostEntity);
			}
		}
    }

}
 
struct hostent* dnsQuery(unsigned char *host)
{
    unsigned char buf[65536],*qname,*reader;
    int i , j , stop;
 
    SOCKET s;
    struct sockaddr_in a;
 
    struct RES_RECORD answers[20],auth[20],addit[20]; //the replies from the DNS server
    struct sockaddr_in dest;
 
    struct DNS_HEADER *dns = NULL;
    struct QUESTION *qinfo = NULL;
	int n;
    struct timeval tv;
    s = socket(AF_INET,SOCK_DGRAM,IPPROTO_UDP); //UDP packet for DNS queries
 
    //Configure the sockaddress structure with information of DNS server
    dest.sin_family=AF_INET;
    dest.sin_port=htons(53);
	dest.sin_addr.s_addr=inet_addr(dns_server);
	//dest.sin_addr.s_addr=inet_addr("208.67.222.222");
    if (dest.sin_addr.s_addr == INADDR_NONE) {
            printf("\nThe IPv4 address entered must be a legal address\n");
            return NULL;
	}
    //Set the DNS structure to standard queries
    dns = (struct DNS_HEADER *)buf;
 
    dns->id = (unsigned short) htons(GetCurrentProcessId());
    dns->qr = 0; //This is a query
    dns->opcode = 0; //This is a standard query
    dns->aa = 0; //Not Authoritative
    dns->tc = 0; //This message is not truncated
    dns->rd = 1; //Recursion Desired
    dns->ra = 0; //Recursion not available! hey we dont have it (lol)
    dns->z = 0;
    dns->ad = 0;
    dns->cd = 0;
    dns->rcode = 0;
    dns->q_count = htons(1); //we have only 1 question
    dns->ans_count = 0;
    dns->auth_count = 0;
    dns->add_count = 0;
 
    //point to the query portion
    qname =(unsigned char*)&buf[sizeof(struct DNS_HEADER)];
 
    ChangetoDnsNameFormat(qname,host);
    qinfo =(struct QUESTION*)&buf[sizeof(struct DNS_HEADER) + (strlen((const char*)qname) + 1)]; //fill it
 
    qinfo->qtype = htons(1); //we are requesting the ipv4 address
    qinfo->qclass = htons(1); //its internet (lol)
 
    printf("\nSending Packet...");
    if(sendto(s,(char*)buf,sizeof(struct DNS_HEADER) + (strlen((const char*)qname)+1) + sizeof(struct QUESTION),0,(struct sockaddr*)&dest,sizeof(dest))==SOCKET_ERROR)
    {
        printf("%d error",WSAGetLastError());
    }
    printf("Sent");
 
    i=sizeof(dest);
    printf("\nReceiving answer...");
	tv.tv_sec = 2;
    tv.tv_usec = 0;
	if (setsockopt(s, SOL_SOCKET, SO_RCVTIMEO, (const char *)&tv, sizeof(tv)) < 0)
    {
        cout<<"\n\nError : Problem with setting timout option"<< endl;
        return NULL;
    }

    n = recvfrom(s,(char*)buf,65536,0,(struct sockaddr*)&dest,&i);
    if(n==SOCKET_ERROR)
    {
        cout<<"\n\nError : code number "<< WSAGetLastError() << endl;
        return NULL;
    }
    else if (n == 0)
    {
        cout<<"\n\nError : Timoueut Reached"<<endl;
        return NULL;
    }
    printf("Received.\n");
 

    // close socket
    closesocket(s);
    
    dns=(struct DNS_HEADER*)buf;
	if(dns->rcode != 0)
	{
		return NULL;
	}
    //move ahead of the dns header and the query field
    reader=&buf[sizeof(struct DNS_HEADER) + (strlen((const char*)qname)+1) + sizeof(struct QUESTION)];
 
    printf("\nThe response contains : ");
    printf("\n %d Questions.",ntohs(dns->q_count));
    printf("\n %d Answers.\n\n",ntohs(dns->ans_count));
  
    //reading answers
    stop=0;
 
    for(i=0;i<ntohs(dns->ans_count);i++)
    {
        answers[i].name=ReadName(reader,buf,&stop);
        reader = reader + stop;
 
        answers[i].resource = (struct R_DATA*)(reader);
        reader = reader + sizeof(struct R_DATA);
 
        if(ntohs(answers[i].resource->type) == 1) //if its an ipv4 address
        {
            answers[i].rdata = (unsigned char*)malloc(ntohs(answers[i].resource->data_len));
 
            for(j=0 ; j<ntohs(answers[i].resource->data_len) ; j++)
            answers[i].rdata[j]=reader[j];
            answers[i].rdata[ntohs(answers[i].resource->data_len)] = '\0';
            reader = reader + ntohs(answers[i].resource->data_len);
 
        }
        else
        {
            answers[i].rdata = ReadName(reader,buf,&stop);
            reader = reader + stop;
        }
 
    }
 
	// Build the hostent struct
	hostent * hostEntity;
    hostEntity = (struct hostent *) malloc( sizeof(struct hostent) );
    hostEntity->h_name = (char *)malloc(strlen((const char *)answers[0].name));
    strncpy(hostEntity->h_name, (const char *)answers[0].name, strlen( (const char *)answers[0].name));
	hostEntity->h_name[strlen((const char *)answers[0].name)] = '\0';
    hostEntity->h_addrtype  = AF_INET;
	hostEntity->h_length    = strlen((const char *)answers[0].name)+1;
    hostEntity->h_addr_list = (char **)malloc(sizeof(char *)*20);
    hostEntity->h_aliases   = (char **)malloc(sizeof(char *)*20);
	j = 0; 
    for(i=0;i<ntohs(dns->ans_count);i++)
    {
            hostEntity->h_addr_list[i] = (char *)malloc(strlen((char *)answers[i].rdata));
            strncpy(hostEntity->h_addr_list[i],  (const char *)answers[i].rdata, strlen( (const char *)answers[i].rdata));
			hostEntity->h_addr_list[i][strlen((const char *)answers[i].rdata)] = '\0';
			if(ntohs(answers[i].resource->type)==5) //Canonical name for an alias
			{
				hostEntity->h_aliases[j] = (char *)malloc(strlen((char *)answers[i].rdata));
				strncpy(hostEntity->h_aliases[j],  (const char *)answers[i].rdata, strlen( (const char *)answers[i].rdata));
				hostEntity->h_aliases[j][strlen((const char *)answers[i].rdata)] = '\0';
				j++;
			}
    }
	hostEntity->h_addr_list[i] = 0;
	hostEntity->h_aliases[j]   = 0;
    return hostEntity;
   
}
unsigned char* ReadName(unsigned char* reader,unsigned char* buffer,int* count)
{
    unsigned char *name;
    unsigned int p=0,jumped=0,offset;
    int i , j;
 
    *count = 1;
    name = (unsigned char*)malloc(256);
 
    name[0]='\0';
 
    //read the names in 3www6google3com format
    while(*reader!=0)
    {
        if(*reader>=192)
        {
            offset = (*reader)*256 + *(reader+1) - 49152; //49152 = 11000000 00000000 ;)
            reader = buffer + offset - 1;
            jumped = 1; //we have jumped to another location so counting wont go up!
        }
        else
        {
            name[p++]=*reader;
        }
 
        reader=reader+1;
 
        if(jumped==0) *count = *count + 1; //if we havent jumped to another location then we can count up
    }
 
    name[p]='\0'; //string complete
    if(jumped==1) 
    {
        *count = *count + 1; //number of steps we actually moved forward in the packet
    }
 
    //now convert 3www6google3com0 to www.google.com
    for(i=0;i<(int)strlen((const char*)name);i++)
    {
        p=name[i];
        for(j=0;j<(int)p;j++)
        {
            name[i]=name[i+1];
            i=i+1;
        }
        name[i]='.';
    }
     
    name[i-1]='\0'; //remove the last dot
     
    return name;
}
//this will convert www.google.com to 3www6google3com ;got it :)
void ChangetoDnsNameFormat(unsigned char* dns,unsigned char* host)
{
    int lock=0 , i;
 
    strcat((char*)host,".");
 
    for(i=0 ; i<(int)strlen((char*)host) ; i++)
    {
        if(host[i]=='.')
        {
            *dns++=i-lock;
            for(;lock<i;lock++)
            {
                *dns++=host[lock];
            }
            lock++; //or lock=i+1;
        }
    }
    *dns++='\0';
}

void PrintDnsResponse(struct hostent* remoteHost)
{
    DWORD dwError;
    int i = 0;
    char **pAlias;
	struct sockaddr_in addr;
    if (remoteHost == NULL) {
		printf("\n\nUnkown Host : Could not find requested host\n\n");
        
    } else {
        printf("Function returned:\n");
        printf("\tOfficial name: %s\n", remoteHost->h_name);
        for (pAlias = remoteHost->h_aliases; *pAlias != 0; pAlias++) {
            printf("\tAlternate name #%d: %s\n", ++i, *pAlias);
        }
        printf("\tAddress type: ");
        switch (remoteHost->h_addrtype) {
        case AF_INET:
            printf("AF_INET\n");
            break;
        case AF_INET6:
            printf("AF_INET6\n");
            break;
            break;
        default:
            printf(" %d\n", remoteHost->h_addrtype);
            break;
        }
        printf("\tAddress length: %d\n", remoteHost->h_length);
		i = 0;
        if (remoteHost->h_addrtype == AF_INET) {
            while (remoteHost->h_addr_list[i] != 0 || i>20) {
                addr.sin_addr.s_addr = *(u_long *) remoteHost->h_addr_list[i++];
                printf("\tIPv4 Address #%d: %s\n", i, inet_ntoa(addr.sin_addr));
            }
        } else if (remoteHost->h_addrtype == AF_INET6)
            printf("\tRemotehost is an IPv6 address\n");
    }
}
 
DNS_Validator::DNS_Validator()
{

}

void DNS_Validator::printError(string errMsg)
{
    cout << "\nERROR: " << errMsg << "\n" << endl;
}



// checks that total string length is less than 254 characters
bool DNS_Validator::checkTotalLength(string inputStr)
{
    if(inputStr.length() < 254)
    {
        return true;
    }
    this->printError("Total Length is larger than 254 characters");
    return false;
}

// checks that the string does not end with a dot
bool DNS_Validator::checkLastCharIsNotSot(string inputStr)
{
    if(*inputStr.rbegin() == '.')
    {
        this->printError("Last character cannot be a dot");
        return false;
    }
    return true;
}

// splits the string into labels (split by .)
// check that each label is less than 64 in length and does not start or end in hyphen
bool DNS_Validator::checkLabels(string inputStr)
{
    istringstream iss(inputStr);
    string token;
    string tokenStart;
    string tokenEnd;
    int    numberOfLables = 0;
    bool atLeastOneDot = false;
    while (getline(iss, token, '.')) {
        if (!token.empty())
        {
            tokenStart = token.at(0);
            tokenEnd   = token.at(token.length() - 1);
            if(tokenStart == "-" || tokenEnd == "-")
            {
                this->printError("URL can not start or end with hyphen");
                return false;
            }
            if(token.length() > 64)
            {
                this->printError("URL Lable lenght is larger than 64 characters");
                return false;
            }
            numberOfLables++;
            atLeastOneDot = true;
        }
    }
    if(!atLeastOneDot)
    {
        this->printError("URL has no valid lables");
        return false;
    }
    if(numberOfLables<2)
    {
        this->printError("Invalid URL");
        return false;
    }
    return true;
}

bool DNS_Validator::checkAlphaNumericDotsOrHyphens(string inputStr)
{
    bool contains_only_alphanum_dot_hyphen = regex_match(inputStr, regex("^[A-Za-z0-9\\.\\-]+$"));
    if(!contains_only_alphanum_dot_hyphen)
    {
        this->printError("URL Cannot contains any character that is not a letter, a number or hyphen");
        return false;
    }
    return true;
}


bool DNS_Validator::checkNotOnlyDigits(string inputStr)
{
    bool contains_letters= regex_search(inputStr, regex("[A-Za-z]+"));
    if(!contains_letters)
    {
        this->printError("URL Cannot contain only numbers");
    }
    return contains_letters;
}

bool DNS_Validator::checkNotEmpty(string inputStr)
{
    if (inputStr.empty())
    {
        this->printError("Empty String Entered");
        return false;
    }

    return true;
}

bool DNS_Validator::checkNotConsequtiveDots(string inputStr)
{
    if(inputStr.find("..") != string::npos){
        this->printError("URL Cannot contain consequtive dots");
        return false;
    }
    return true;
}

bool DNS_Validator::isValid(string inputStr)
{
    if(!this->checkNotEmpty(inputStr))                  {return false;}
    if(!this->checkNotConsequtiveDots(inputStr))        {return false;}
    if(!this->checkAlphaNumericDotsOrHyphens(inputStr)) {return false;}
    if(!this->checkNotOnlyDigits(inputStr))             {return false;}
    if(!this->checkTotalLength(inputStr))               {return false;}
    if(!this->checkLabels(inputStr))                    {return false;}
    if(!this->checkLastCharIsNotSot(inputStr))          {return false;}
    return true;
}
