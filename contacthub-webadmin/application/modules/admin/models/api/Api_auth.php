<?php
require APPPATH.'vendor/autoload.php';
use GuzzleHttp\Client;
class Api_auth
{
    public function __construct(){
        $this->app =& get_instance();
        $this->client_gateway = new Client([
            'base_uri' => $this->app->config->item('BASE_URI_API_GATEWAY'),
            'timeout'  => 120.0,
        ]);
        $this->client = new Client([
            'base_uri' => $this->app->config->item('BASE_URI_API'),
            'timeout'  => 120.0,
        ]);
    }
    public function login($body){
        try {
            $response   = $this->client_gateway->request('POST', '/admin/login',[
                'headers'=> [
                    'Content-Type' => 'application/json'
                ],
                'json' => $body 
            ]);
            $result     = json_decode($response->getBody()->getContents());
        }catch (GuzzleHttp\Exception\BadResponseException $e) {
            $response = $e->getResponse();
            $result     = json_decode($response->getBody()->getContents());
        }
        return $result;
    }

    public function logout($apiKey){
        try {
            $response   = $this->client_gateway->request('POST', '/admin/logout',[
                'headers'=> [
                    'Content-Type' => 'application/json',
                    'X-API-KEY' => $apiKey,
                ], 
            ]);
            $result     = json_decode($response->getBody()->getContents());
        }catch (GuzzleHttp\Exception\BadResponseException $e) {
            $response = $e->getResponse();
            $result     = json_decode($response->getBody()->getContents());
        }
        return $result;
    }
    public function update_expired_token($accessToken){
        try {
            $response   = $this->client->request('POST', '/token/update',[
                'headers'=> [
                    'Content-Type' => 'application/json'
                ],
                'json' => [
                    'AccessToken' => $accessToken
                ]
            ]);
            $result     = json_decode($response->getBody()->getContents());
        }catch (GuzzleHttp\Exception\BadResponseException $e) {
            $response = $e->getResponse();
            $result     = json_decode($response->getBody()->getContents());
        }
        return $result;
    }
    public function clear_token($accessToken){
        try {
            $response   = $this->client->request('POST', '/token/clear',[
                'headers'=> [
                    'Content-Type' => 'application/json'
                ],
                'json' => [
                    'AccessToken' => $accessToken
                ]
            ]);
            $result     = json_decode($response->getBody()->getContents());
        }catch (GuzzleHttp\Exception\BadResponseException $e) {
            $response = $e->getResponse();
            $result     = json_decode($response->getBody()->getContents());
        }
        return $result;
    }
    public function clear_token_user($filter=[]){
        try {
            $response   = $this->client->request('POST', '/token/user/clear',[
                'headers'=> [
                    'Content-Type' => 'application/json'
                ],
                'json' => $filter
            ]);
            $result     = json_decode($response->getBody()->getContents());
        }catch (GuzzleHttp\Exception\BadResponseException $e) {
            $response = $e->getResponse();
            $result     = json_decode($response->getBody()->getContents());
        }
        return $result;
    }

    public function get_profile_by_token($accessToken){
        try {
            $response   = $this->client->request('GET', '/user/profile/token',[
                'headers'=> [
                    'Content-Type' => 'application/json'
                ],
                'query' => [
                    'accessToken' => $accessToken
                ] 
            ]);
            $result     = json_decode($response->getBody()->getContents());
        }catch (GuzzleHttp\Exception\BadResponseException $e) {
            $response = $e->getResponse();
            $result     = json_decode($response->getBody()->getContents());
        }
        return $result;
    }
}
