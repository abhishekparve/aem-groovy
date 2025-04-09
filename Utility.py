import pandas as pd
import requests
from urllib.parse import quote

# Loding the excel file
excel_file = "testing_123.xlsx"
sheet_name = "Sheet1"

df = pd.read_excel(excel_file, sheet_name=sheet_name, engine='openpyxl')

#Bearer token
bearer_token = "eyJhbGciOiJSUzI1NiIsIng1dSI6Imltc19uYTEta2V5LWF0LTEuY2VyIiwia2lkIjoiaW1zX25hMS1rZXktYXQtMSIsIml0dCI6ImF0In0.eyJpZCI6IjE3MjEzMTMwOTk2MzdfZjRhNWQ3NWQtZGZjYS00ZjQzLThhZGItODYyODc5YTk0ZDc5X3VlMSIsInR5cGUiOiJhY2Nlc3NfdG9rZW4iLCJjbGllbnRfaWQiOiJkZXYtY29uc29sZS1wcm9kIiwidXNlcl9pZCI6IkM4QkY0MjUyNjE2NDkwNTkwQTQ5NUVGRUBhN2VjNjI1ZjVkNWIwNGU2MGE0OTVjYzciLCJzdGF0ZSI6Ijd0WDJsZ1dvWTVHMGN4NWFCcWhIRk5YbSIsImFzIjoiaW1zLW5hMSIsImFhX2lkIjoiQzhCRjQyNTI2MTY0OTA1OTBBNDk1RUZFQGE3ZWM2MjVmNWQ1YjA0ZTYwYTQ5NWNjNyIsImN0cCI6MCwiZmciOiJZVDdZUFpHNEZQUDVNSFVLRk1RVllIQUFUUSIsInNpZCI6IjE3MTAyMzQ1NjU1NTNfNWRlZjg2MzItMDhkMy00ZDM2LWI3Y2QtMTdkZmFiY2Q4ZTlmX3VlMSIsInJ0aWQiOiIxNzIxMzEzMDk5NjM4XzBmNWM2ZGZkLTMxNmQtNGMyMC1iOGZkLTc2MWEyZmNiZGVhZV91ZTEiLCJtb2kiOiJjODRkNzZkZiIsInBiYSI6Ik1lZFNlY05vRVYsTG93U2VjIiwicnRlYSI6IjE3MjI1MjI2OTk2MzgiLCJleHBpcmVzX2luIjoiODY0MDAwMDAiLCJzY29wZSI6IkFkb2JlSUQsb3BlbmlkLHJlYWRfb3JnYW5pemF0aW9ucyxhZGRpdGlvbmFsX2luZm8ucHJvamVjdGVkUHJvZHVjdENvbnRleHQiLCJjcmVhdGVkX2F0IjoiMTcyMTMxMzA5OTYzNyJ9.LxKOUqTDYq3SxJL1VIG3UO15JdVJyFZK3zw2Hjc5De1AkFRA4mgLuG70_UVUsWN3fm169ggmUHZjNV9OfuWaWmswBH2crFOBRIllQ7zRnSLUVmL__c78FPxNXMrEbgmfHo7bVVeLfdZ71srtyR6NUEtQr2XT8cHBXxfu2RV8unl8HzTFYq93eQcIqba5RYelLPPeE82MYLNtj8SZmG3S_xzZD5ZdRF8WzfG5_tCGQMd2Ay2TRjXUHQeTWcaoqlIsNjYaxySKEBOLsLpW4xCZu4WPOg1YP4MRZood__-g6rPzpuEO9rzjcTST7WtlxAPOF1j8EfWOf9aq7QqPaJxgIg"
base_url = 'https://dev-console-ns-team-aem-cm-prd-n19360.ethos15-prod-va7.dev.adobeaemcloud.com/api/browser/all%20publishers-cm-p14102-e70270/resources.json'

def send_get_request(path):
    headers = {
        'Authorization' : f'Bearer {bearer_token}'
    }
    path = path.strip()
    quoted_path = quote(path, safe='/')
    quoted_url = f"{base_url}?path={quoted_path}&depth=1&ancestors=false"
    url = f"{base_url}?path={path}&depth=1&ancestors=false"
    print(url)
    response = requests.get(url, headers=headers)
    if response.status_code == 200:
        return response
    elif response.status_code == 404:
        quoted_response = requests.get(quoted_url, headers=headers)
        if quoted_response.status_code == 200:
            return quoted_response 
    # print(response)
    return response

failed_paths = []
passed_paths = []

for index, row in df.iterrows():
    path = row['pathFromFile']
    path=path.strip()
    response = send_get_request(path)
    if response.status_code == 404:
        failed_paths.append(path)
    elif response.status_code == 200:
        passed_paths.append(path)
    print(response.status_code)


with open('failed_paths.txt', 'w', encoding='utf-8') as f:
    for failed_path in failed_paths:
        f.write(f"{failed_path}\n")

with open('passed_paths.txt', 'w', encoding='utf-8') as p:
    for passed_path in passed_paths:
        p.write(f"{passed_path}\n")

print(f"Passed paths : {len(passed_paths)}")
print(f"Failed paths : {len(failed_paths)}")




