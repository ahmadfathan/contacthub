<?php
require APPPATH.'vendor/autoload.php';
use GuzzleHttp\Client;

class Api_greeting
{
    public function __construct(){
        $this->app =& get_instance();

        $this->client = new Client([
            'base_uri' => $this->app->config->item('BASE_URI_API'),
            'timeout'  => 120.0,
        ]);
    }
    
    public function getAll($filter=[]){
        try {
            $response   = $this->client->request('GET', '/greeting',[
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
            $column_order = ['','GreetingId'];
            // Response 200 
            if (isset($filter['order_column'])){
                $filter['order_by'] = [
                    $column_order[$filter['order_column']] => $filter['order_dir']
                ];
                unset($filter['order_column']);
                unset($filter['order_dir']);
            }
            $response   = $this->client->request('GET', '/greeting/paginate',[
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
            $response   = $this->client->request('POST', '/greeting',[
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
    
    public function delete($data=[]){
        try {
            $response   = $this->client->request('DELETE', '/greeting',[
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
