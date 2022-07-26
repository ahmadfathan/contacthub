<?php
require APPPATH.'vendor/autoload.php';
use GuzzleHttp\Client;

class Api_customer
{
    public function __construct(){
        $this->app =& get_instance();

        $this->client = new Client([
            'base_uri' => $this->app->config->item('BASE_URI_API'),
            'timeout'  => 120.0,
        ]);
    }
    public function by_id($id){
        try {
            // Response 200 
            $response   = $this->client->request('GET', '/user/'.$id);
            $result     = json_decode($response->getBody()->getContents());
        }catch (GuzzleHttp\Exception\BadResponseException $e) {
            $response = $e->getResponse();
            $result     = json_decode($response->getBody()->getContents());
        }
        return $result;
    }
    public function count(){
        try {
            // Response 200 
            $response   = $this->client->request('GET', '/user/count');
            $result     = json_decode($response->getBody()->getContents());
        }catch (GuzzleHttp\Exception\BadResponseException $e) {
            $response = $e->getResponse();
            $result     = json_decode($response->getBody()->getContents());
        }
        return $result;
    }
    public function getListStatus(){
        try {
            $response   = $this->client->request('GET', '/user/status');
            $result     = json_decode($response->getBody()->getContents());
        }catch (GuzzleHttp\Exception\BadResponseException $e) {
            $response = $e->getResponse();
            $result     = json_decode($response->getBody()->getContents());
        }
        return $result;
    }
    public function getDownline($filter){
        try {
            $response   = $this->client->request('POST', '/downline',[
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
    public function getUpline($user_id){
        try {
            $response   = $this->client->request('POST', '/upline',[
                'headers'=> [
                    'Content-Type' => 'application/json'
                ],
                'json' => [
                    'UserId' => $user_id
                ]
            ]);
            $result     = json_decode($response->getBody()->getContents());
        }catch (GuzzleHttp\Exception\BadResponseException $e) {
            $response = $e->getResponse();
            $result     = json_decode($response->getBody()->getContents());
        }
        return $result;
    }
    public function getAll($filter=[]){
        try {
            $response   = $this->client->request('GET', '/customer',[
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
    public function getAllNotPaginate($filter=[]){
        try {
            $response   = $this->client->request('GET', '/customer/all',[
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
    
    public function get_datatables($filter=[]){
        try {
            $column_order = ['','Username','Nickname','Email','Role.Name','Status.Description'];
            // Response 200 
            if (isset($filter['order_column'])){
                $filter['order_by'] = [
                    $column_order[$filter['order_column']] => $filter['order_dir']
                ];
                unset($filter['order_column']);
                unset($filter['order_dir']);
            }
            $response   = $this->client->request('GET', '/customer',[
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
    public function insert($data=[]){
        try {
            $response   = $this->client->request('POST', '/user',[
                'headers'=> [
                    'Content-Type' => 'application/json'
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
    public function update($data=[]){
        try {
            $response   = $this->client->request('PUT', '/customer',[
                'headers'=> [
                    'Content-Type' => 'application/json'
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
    public function delete($data){
        try {
            $response   = $this->client->request('DELETE', '/customer',[
                'headers'=> [
                    'Content-Type' => 'application/json'
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
