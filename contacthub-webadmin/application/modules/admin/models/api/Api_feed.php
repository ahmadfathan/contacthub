<?php
require APPPATH.'vendor/autoload.php';
use GuzzleHttp\Client;

class Api_feed
{
    public function __construct(){
        $this->app =& get_instance();

        $this->client = new Client([
            'base_uri' => $this->app->config->item('BASE_URI_API'),
            'timeout'  => 120.0,
        ]);
    }
    public function getAll($user_id,$filter=[]){
        try {
            $response   = $this->client->request('GET', '/feed',[
                'headers'=> [
                    'Content-Type' => 'application/json',
                    'X-Auth-UserId' => $user_id
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
    public function getAllNotPaginate($UserId,$filter=[]){
        try {
            $response   = $this->client->request('GET', '/feed/all',[
                'headers'=> [
                    'Content-Type' => 'application/json',
                    'X-Auth-UserId' => $UserId
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
    
    public function get_datatables($UserId,$filter=[]){
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
            $response   = $this->client->request('GET', '/feed',[
                'headers'=> [
                    'Content-Type' => 'application/json',
                    'X-Auth-UserId' => $UserId
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
            $response   = $this->client->request('POST', '/feed',[
                'headers'=> [
                    'X-Auth-UserId' => $UserId
                ],
                'multipart' => $data 
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
            $response   = $this->client->request('PUT', '/feed',[
                'headers'=> [
                    'X-Auth-UserId' => $UserId
                ],
                'multipart' => $data 
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
            $response   = $this->client->request('DELETE', '/feed',[
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

    public function update_status($UserId,$data=[]){
        try {
            $response   = $this->client->request('PUT', '/feed/status',[
                'headers'=> [
                    'X-Auth-UserId' => $UserId
                ],
                'form_params' => $data 
            ]);
            $result     = json_decode($response->getBody()->getContents());
        }catch (GuzzleHttp\Exception\BadResponseException $e) {
            $response = $e->getResponse();
            $result     = json_decode($response->getBody()->getContents());
        }
        return $result;
    }

}
