
# Sqlite3 tool

## Installation
```declarative
$ sudo apt-get update && sudo apt-get install sqlite3

$ sqlite3 --version
3.37.2 2022-01-06 13:25:41 872ba256cbf61d9290b571c0e6d82a20c224ca3ad82971edc46b29818d5dalt1
```

## Dump DB
```declarative
$ sqlite3 db/history.db .dump

$ sqlite3 -header -column db/history.db "SELECT * FROM jwt_sign_history;"
```

## Interactive Mode
```declarative
$ sqlite3 db/history.db
SQLite version 3.37.2 2022-01-06 13:25:41
Enter ".help" for usage hints.

sqlite> .tables
jwt_sign_history

sqlite> .headers on

sqlite> .mode column

sqlite> SELECT * FROM jwt_sign_history;
id  failure_reason  original_jwt  signed_jwt_result                                                                                                                                                                                                                                                                                                                                                                                                                                                                            success  timestamp    
--  --------------  ------------  -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------  -------  -------------
1                                 eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIn0.oinp60DN0nd_u4SKBdvgqHgoTblARY_GjDfTLcvrgvpFKWvZmFQExdq4FK45Fd55Z4jzLkhDrcR1B4PWW5wKT1rB6YEZE5LYJp_1QaqX4AbbPbV8RrbjFgO_o9eeVkijMlsVll-NvNemAWfhxaHhrypEQ3X7ijZ28JvyBZ24FK5PoCzJzA6fmOtbk3tHkm_dqfTQCBjAsVyXGq_PWyxRuuldd9FcdNMRW-sk8-VkhzFemwpIqyTD58IOR_xfB8ulFt20E9yFvPBSZ2QX4FRJj6OKz8vLl9OWyQmruVF3LUGNDUCPL1Vwt0die7JlsDZrNOeTLF9cwo2n3sKcHU86pQ                                                              1        1753836550842
2                                 eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJsaWNlbnNlIiwibmFtZSI6IkpleSBjb21wYW55IiwiYXBwcyI6WyJhcHAxIiwiYXBwMiJdLCJleHBpcmUiOiIyMDI1LzEyLzMxIn0.AIoWGPlFGUMtvg_qYW0qJWwev7yAEIOFWMSvmvcj8emgTskaJGsOCk7dnwv3_NhXY0tnYj2YoEKRZE8Etq24D92zGjucqzwTCyPcAJdeBDvmUxs3ntVF8n9s_j8HS4eN4BYIw2NCv7WSxcuEl8ZbEZJtnQLxJ6wZxxEX29Aj6WZs4kIuy_fW-r32smZ0I_x7chF7eRNJwNDMq9ziq03K0TMyCs0IjvjvxvRTxw726fzOvem9mA0GEOwEZZIbo6iRFAU6hNRNInhPWN1G2RglwVHzsmmOvV8YAk7oC5Gkee4lGEBtajwqBemE2P4DX4VH04dtSA-QzhrfUo4GZcXDfQ  1        1753836673479
3                                 eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJsaWNlbnNlIiwibmFtZSI6IkpleSBjb21wYW55IiwiYXBwcyI6WyJhcHAxIiwiYXBwMiJdLCJleHBpcmUiOiIyMDI1LzEyLzMxIn0.AIoWGPlFGUMtvg_qYW0qJWwev7yAEIOFWMSvmvcj8emgTskaJGsOCk7dnwv3_NhXY0tnYj2YoEKRZE8Etq24D92zGjucqzwTCyPcAJdeBDvmUxs3ntVF8n9s_j8HS4eN4BYIw2NCv7WSxcuEl8ZbEZJtnQLxJ6wZxxEX29Aj6WZs4kIuy_fW-r32smZ0I_x7chF7eRNJwNDMq9ziq03K0TMyCs0IjvjvxvRTxw726fzOvem9mA0GEOwEZZIbo6iRFAU6hNRNInhPWN1G2RglwVHzsmmOvV8YAk7oC5Gkee4lGEBtajwqBemE2P4DX4VH04dtSA-QzhrfUo4GZcXDfQ  1        1753836681218
4                                 eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJsaWNlbnNlIiwibmFtZSI6IkpleSBjb21wYW55IiwiYXBwcyI6WyJhcHAxIiwiYXBwMiJdLCJleHBpcmUiOiIyMDI1LzEyLzMxIn0.AIoWGPlFGUMtvg_qYW0qJWwev7yAEIOFWMSvmvcj8emgTskaJGsOCk7dnwv3_NhXY0tnYj2YoEKRZE8Etq24D92zGjucqzwTCyPcAJdeBDvmUxs3ntVF8n9s_j8HS4eN4BYIw2NCv7WSxcuEl8ZbEZJtnQLxJ6wZxxEX29Aj6WZs4kIuy_fW-r32smZ0I_x7chF7eRNJwNDMq9ziq03K0TMyCs0IjvjvxvRTxw726fzOvem9mA0GEOwEZZIbo6iRFAU6hNRNInhPWN1G2RglwVHzsmmOvV8YAk7oC5Gkee4lGEBtajwqBemE2P4DX4VH04dtSA-QzhrfUo4GZcXDfQ  1        1753836692459
5                                 eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIn0.eC5os5dDlpiVCEJNRyxEyTBCXGxoc9H64BJBM-079_dsT7aZLwaIPWqXa3IIBkcBJ3mMCHsDZpSfpJK4xj09KsLNgfCxCZowpgdXCG2UsUEk2disYnemeVHiyMjmyCej66CoO68VHaODFMyP8Uv73_hS9jZHpN4jTgEYuKLwMEuOozVg69kU65n19b1atdw0X7eDo4J-wRxxxrW_YA4jUeheqWFKYj9XVHiZs3xUWfzLmuyNOWr5ueYfyPgmgTxk12_kevlDhhLocvU0B9Q3sFEWDxkDn2rPW9Q8pfzOZffQCKvfd7XVKyZ9wKfnchXCW7JEidQb9FJvemp98UlmRA                                                              1        1753837429883
6                                 eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIn0.hjNfWvFgJtoO55yfelcGFuipvXKwPh_yQdK6AmcHutz6vyeDCmwMJpBuxJmjfybKfHuwudPK1DkxBI0c_Okj2bmZE7pLtGs74nxWT0sS7h4sqcy9LzGJ9Y8wFbtTSkIhh7hMosv3CJwcYnqtioJrXILx8whuSXPawp_1t0LX1M2CxkeT9zdDKpF1VVj38iKEmakI0l7tIPCwyGU6PeItNbl_f9QhwBWWOErWaO279qAYikRpIh9o3S3uGz5-IVwSsMPv9UP20-pPorj_NJNHL0xho7BjKcgQ6-zyRqDF9fEdJGEy5k-AxX9I_wN0x5eZcZct7aunyuOl2GCYYEGpeQ                                                              1        1753837964515
7                                 eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJsaWNlbnNlIiwibmFtZSI6IkpleSBjb21wYW55IiwiYXBwcyI6WyJhcHAxIiwiYXBwMiJdLCJleHBpcmUiOiIyMDI1LzEyLzMxIn0.AIoWGPlFGUMtvg_qYW0qJWwev7yAEIOFWMSvmvcj8emgTskaJGsOCk7dnwv3_NhXY0tnYj2YoEKRZE8Etq24D92zGjucqzwTCyPcAJdeBDvmUxs3ntVF8n9s_j8HS4eN4BYIw2NCv7WSxcuEl8ZbEZJtnQLxJ6wZxxEX29Aj6WZs4kIuy_fW-r32smZ0I_x7chF7eRNJwNDMq9ziq03K0TMyCs0IjvjvxvRTxw726fzOvem9mA0GEOwEZZIbo6iRFAU6hNRNInhPWN1G2RglwVHzsmmOvV8YAk7oC5Gkee4lGEBtajwqBemE2P4DX4VH04dtSA-QzhrfUo4GZcXDfQ  1        1753838041245
8                                 eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIn0.Yf3ehiiy2BGzEkZLiYd18L2_K2pzL85K0a4dXKzs4N_ITVVdfNGQTFQ-8m8XjmaqYZgv9TXnNybHjkOdWANRp9ZyQDxG8JhPUVXu8th5NLRGZHMihcdg1zeHsR7ZKhzar_wocV2OO7APYkAUVj_Gf-U29DwZJLtZ1f8VqxFs48F40GhAvuhYeCh_pJO-8J3cacMn8woyZ7ViTu18QoKUVOOFYQSxz0fKVzGKoBNgA3U_-is65hc_i_Tzphx6e0cS8940LETpEQkAMS_4ykUrC4xXgpuK0x4-u6GhOdBiXONHSU7osorZQIfbKZ7p8xUr0BXiwbWXKrYfVUmvmgN-Jw                                                              1        1753838546951

```

