
import torch
from transformers import AutoModelForCausalLM, AutoTokenizer
import time
import requests
from zapv2 import ZAPv2
from requests.exceptions import ProxyError
import json
import requests

# Load pre-trained LLM model and tokenizer
model_name = "cognitivecomputations/dolphin-2.8-experiment26-7b-preview"
tokenizer = AutoTokenizer.from_pretrained(model_name, trust_remote_code=True)
model = AutoModelForCausalLM.from_pretrained(model_name,trust_remote_code=True)
# Initialize ZAP API client with API key
api_key = 'vh7bqrauhothh2b0en7r53se5i'
zap = ZAPv2(apikey=api_key)


# Take target URL as input
target = input("Please enter the target URL to scan: ").strip()
code = input("Please enter the website code language: ").strip()
# Crawl and scan web application
#zap.spider.scan(target)
#scan = zap.ascan.scan(target)

#print(f'Scan started with ID: {scan}')

# Check the status of the scan
print("✅ Scan results is generated.")
scan_results  =  [{'sourceid': '3', 'other': '', 'method': 'GET', 'evidence': '', 'pluginId': '10038', 'cweid': '693', 'confidence': 'High', 'sourceMessageId': 25, 'wascid': '15', 'description': 'Content Security Policy (CSP) is an added layer of security that helps to detect and mitigate certain types of attacks, including Cross Site Scripting (XSS) and data injection attacks. These attacks are used for everything from data theft to site defacement or distribution of malware. CSP provides a set of standard HTTP headers that allow website owners to declare approved sources of content that browsers should be allowed to load on that page — covered types are JavaScript, CSS, HTML frames, fonts, images and embeddable objects such as Java applets, ActiveX, audio and video files.', 'messageId': '25', 'inputVector': '', 'url': 'http://34.229.132.148:5000/sitemap.xml', 'tags': {'OWASP_2021_A05': 'https://owasp.org/Top10/A05_2021-Security_Misconfiguration/', 'POLICY_QA_STD': '', 'POLICY_PENTEST': '', 'CWE-693': 'https://cwe.mitre.org/data/definitions/693.html', 'OWASP_2017_A06': 'https://owasp.org/www-project-top-ten/2017/A6_2017-Security_Misconfiguration.html'}, 'reference': 'https://developer.mozilla.org/en-US/docs/Web/Security/CSP/Introducing_Content_Security_Policy\nhttps://cheatsheetseries.owasp.org/cheatsheets/Content_Security_Policy_Cheat_Sheet.html\nhttps://www.w3.org/TR/CSP/\nhttps://w3c.github.io/webappsec-csp/\nhttps://web.dev/articles/csp\nhttps://caniuse.com/#feat=contentsecuritypolicy\nhttps://content-security-policy.com/', 'solution': 'Ensure that your web server, application server, load balancer, etc. is configured to set the Content-Security-Policy header.', 'alert': 'Content Security Policy (CSP) Header Not Set', 'param': '', 'attack': '', 'name': 'Content Security Policy (CSP) Header Not Set', 'risk': 'Medium', 'id': '0', 'alertRef': '10038-1'}, {'sourceid': '3', 'other': '', 'method': 'GET', 'evidence': '', 'pluginId': '10038', 'cweid': '693', 'confidence': 'High', 'sourceMessageId': 22, 'wascid': '15', 'description': 'Content Security Policy (CSP) is an added layer of security that helps to detect and mitigate certain types of attacks, including Cross Site Scripting (XSS) and data injection attacks. These attacks are used for everything from data theft to site defacement or distribution of malware. CSP provides a set of standard HTTP headers that allow website owners to declare approved sources of content that browsers should be allowed to load on that page — covered types are JavaScript, CSS, HTML frames, fonts, images and embeddable objects such as Java applets, ActiveX, audio and video files.', 'messageId': '22', 'inputVector': '', 'url': 'http://34.229.132.148:5000/robots.txt', 'tags': {'OWASP_2021_A05': 'https://owasp.org/Top10/A05_2021-Security_Misconfiguration/', 'POLICY_QA_STD': '', 'POLICY_PENTEST': '', 'CWE-693': 'https://cwe.mitre.org/data/definitions/693.html', 'OWASP_2017_A06': 'https://owasp.org/www-project-top-ten/2017/A6_2017-Security_Misconfiguration.html'}, 'reference': 'https://developer.mozilla.org/en-US/docs/Web/Security/CSP/Introducing_Content_Security_Policy\nhttps://cheatsheetseries.owasp.org/cheatsheets/Content_Security_Policy_Cheat_Sheet.html\nhttps://www.w3.org/TR/CSP/\nhttps://w3c.github.io/webappsec-csp/\nhttps://web.dev/articles/csp\nhttps://caniuse.com/#feat=contentsecuritypolicy\nhttps://content-security-policy.com/', 'solution': 'Ensure that your web server, application server, load balancer, etc. is configured to set the Content-Security-Policy header.', 'alert': 'Content Security Policy (CSP) Header Not Set', 'param': '', 'attack': '', 'name': 'Content Security Policy (CSP) Header Not Set', 'risk': 'Medium', 'id': '1', 'alertRef': '10038-1'}, {'sourceid': '3', 'other': '', 'method': 'GET', 'evidence': '', 'pluginId': '10020', 'cweid': '1021', 'confidence': 'Medium', 'sourceMessageId': 24, 'wascid': '15', 'description': "The response does not protect against 'ClickJacking' attacks. It should include either Content-Security-Policy with 'frame-ancestors' directive or X-Frame-Options.", 'messageId': '24', 'inputVector': '', 'url': 'http://34.229.132.148:5000/', 'tags': {'OWASP_2021_A05': 'https://owasp.org/Top10/A05_2021-Security_Misconfiguration/', 'POLICY_QA_STD': '', 'POLICY_PENTEST': '', 'CWE-1021': 'https://cwe.mitre.org/data/definitions/1021.html', 'WSTG-v42-CLNT-09': 'https://owasp.org/www-project-web-security-testing-guide/v42/4-Web_Application_Security_Testing/11-Client-side_Testing/09-Testing_for_Clickjacking', 'OWASP_2017_A06': 'https://owasp.org/www-project-top-ten/2017/A6_2017-Security_Misconfiguration.html'}, 'reference': 'https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/X-Frame-Options', 'solution': 'Modern Web browsers support the Content-Security-Policy and X-Frame-Options HTTP headers. Ensure one of them is set on all web pages returned by your site/app.\nIf you expect the page to be framed only by pages on your server (e.g. it\'s part of a FRAMESET) then you\'ll want to use SAMEORIGIN, otherwise if you never expect the page to be framed, you should use DENY. Alternatively consider implementing Content Security Policy\'s "frame-ancestors" directive.', 'alert': 'Missing Anti-clickjacking Header', 'param': 'x-frame-options', 'attack': '', 'name': 'Missing Anti-clickjacking Header', 'risk': 'Medium', 'id': '2', 'alertRef': '10020-1'}]

report = []
new_report = scan_results[:1]
i=0
for item in new_report:
    i=i+1
    description = item["description"]
    reference = item["reference"]
    solution_provided = item["solution"]
    url = "http://3.236.245.215/api/generate"
    payload = {
     "model": "llama3",
     "prompt": (
      f"Vulnerability Reference: {reference}\n"
      f"Description: {description}\n"
      f"ZAP Recommendation: {solution_provided}\n"
      f"Fix Required: Please provide the necessary code changes and specify the file name where the changes should be applied with the updated code in the repository '{code}'"
     ),
     "stream": False
    }

    solution = requests.post(url, json=payload)


    response_json = solution.json()
    readable_output = response_json["response"].encode().decode("unicode_escape")
    print(readable_output)

    report_entry = {
        "Vulnerability_No": i,
        "Name" : item["name"],
        "url": item["url"],
        "risk": item["risk"],
        "tags" : item["tags"],
        "solution": readable_output
    }
    print(f'✅ Solution is generated for Vulnerability_No: {i}.')
    report.append(report_entry)
# Save DAST report to file

with open("dast_report.txt", "w") as f:
    for entry in report:
        f.write(f"Vulnerability No: {entry['Vulnerability_No']}\n")
        f.write(f"Name: {entry['Name']}\n")
        f.write(f"URL: {entry['url']}\n")
        f.write(f"Risk: {entry['risk']}\n")
        f.write(f"Tags: {', '.join(entry['tags'])}\n")
        f.write(f"{entry['solution']}\n")
        f.write("\n" + "-"*80 + "\n")
line_to_remove = "What is the best mitigation strategy for this vulnerability with steps for python?"

with open("dast_report.txt", "r", encoding="utf-8") as file:
    lines = file.readlines()

with open("dast_report.txt", "w", encoding="utf-8") as file:
    for line in lines:
        if line.strip() != line_to_remove:
           file.write(line)


print("✅ DAST report generated and saved to dast_report.txt.")                                                                                                                                        1,1           Top
