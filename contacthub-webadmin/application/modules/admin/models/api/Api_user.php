<?php
require APPPATH.'vendor/autoload.php';
use GuzzleHttp\Client;

class Api_user
{
    public function __construct(){
        $this->app =& get_instance();

        $this->client = new Client([
            'base_uri' => $this->app->config->item('BASE_URI_API'),
            'timeout'  => 60.0,
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
    public function getAll($filter=[]){
        try {
            $response   = $this->client->request('GET', '/user/all',[
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
            $response   = $this->client->request('POST', '/user/paginate',[
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
            $response   = $this->client->request('POST', '/users',[
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
    public function update_password($id,$data=[]){
        try {
            $response   = $this->client->request('PUT', '/user/password/update/'.$id,[
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
    public function update($id,$data=[]){
        try {
            $response   = $this->client->request('PATCH', '/user/'.$id,[
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
    public function delete($id){
        try {
            $response   = $this->client->request('DELETE', '/user/'.$id,[
                'headers'=> [
                    'Content-Type' => 'application/json'
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
