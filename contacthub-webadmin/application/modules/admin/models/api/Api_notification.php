<?php
require APPPATH.'vendor/autoload.php';

use GuzzleHttp\Client;

class Api_notification
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
    public function getAll($user_id,$filter=[]){
        try {
            $response   = $this->client->request('GET', '/notification',[
                'headers'=> [
                    'Content-Type' => 'application/json',
                    'X-Auth-UserId' => $user_id,
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
    public function get_datatables($user_id,$filter=[]){
        try {
            $column_order = ['','Name'];
            // Response 200 
            if (isset($filter['order_column'])){
                $filter['order_by'] = [
                    $column_order[$filter['order_column']] => $filter['order_dir']
                ];
                unset($filter['order_column']);
                unset($filter['order_dir']);
            }
            $response   = $this->client->request('POST', '/notification/paginate',[
                'headers'=> [
                    'Content-Type' => 'application/json',
                    'X-Auth-UserId' => $user_id,
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
    public function insert($UserId,$data=[]){
        try {
            $response   = $this->client->request('POST', '/notification',[
                'headers'=> [
                    'Content-Type' => 'application/json',
                    'X-Auth-UserId' => $UserId
                ],
                'json' => $data 
            ]);
            $result     = json_decode($response->getBody()->getContents());
        }catch (GuzzleHttp\Exception\BadResponseException $e) {
            $response = $e->getResponse();
            $result     = json_decode($response->getBody()->getContents());
        }
        return $result;
    }
    public function update($UserId,$data=[]){
        try {
            $response   = $this->client->request('PUT', '/notification',[
                'headers'=> [
                    'Content-Type' => 'application/json',
                    'X-Auth-UserId' => $UserId
                ],
                'json' => $data 
            ]);
            $result     = json_decode($response->getBody()->getContents());
        }catch (GuzzleHttp\Exception\BadResponseException $e) {
            $response = $e->getResponse();
            $result     = json_decode($response->getBody()->getContents());
        }
        return $result;
    }
    public function delete($UserId,$data){
        try {
            $response   = $this->client->request('DELETE', '/notification',[
                'headers'=> [
                    'Content-Type' => 'application/json',
                    'X-Auth-UserId' => $UserId
                ],
                'json' => $data 

            ]);
            $result     = json_decode($response->getBody()->getContents());
        }catch (GuzzleHttp\Exception\BadResponseException $e) {
            $response = $e->getResponse();
            $result     = json_decode($response->getBody()->getContents());
        }
        return $result;
    }
}
